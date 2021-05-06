package ink.ptms.chemdah.core.quest.meta

import com.google.common.base.Enums
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Id
import ink.ptms.chemdah.core.quest.QuestContainer
import ink.ptms.chemdah.core.quest.Template
import ink.ptms.chemdah.core.quest.meta.MetaAlias.Companion.alias
import ink.ptms.chemdah.core.quest.meta.MetaLabel.Companion.label
import ink.ptms.chemdah.util.*
import io.izzel.taboolib.cronus.util.Time
import io.izzel.taboolib.kotlin.kether.KetherShell
import io.izzel.taboolib.util.Coerce
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.meta.MetaAliases
 *
 * @author sky
 * @since 2021/3/1 11:47 下午
 */
@Suppress("UNCHECKED_CAST")
@Id("control")
@MetaType(MetaType.Type.MAP_LIST)
class MetaControl(source: List<Map<String, Any>>, questContainer: QuestContainer) : Meta<List<Map<String, Any>>>(source, questContainer) {

    val control = ArrayList<Control>()

    init {
        source.forEach { map ->
            if (map["$"] != null) {
                control += ControlAgent(map["$"]!!.asList())
            } else {
                val type = map["type"].toString().toLowerCase()
                when {
                    type == "coexist" -> {
                        ControlCoexist(map["alias"].asInt(), map["label"].asMap().map { it.key to it.value.asInt() }.toMap())
                    }
                    type.startsWith("repeat") -> {
                        val trigger = Trigger.fromName(type.substring("repeat".length).trim())
                        ControlRepeat(trigger, map["amount"].asInt(), map["period"]?.toString()?.toTime(), map["group"]?.toString())
                    }
                    type.startsWith("cooldown") -> {
                        val trigger = Trigger.fromName(type.substring("cooldown".length).trim())
                        ControlCooldown(trigger, map["time"]?.toString()?.toTime() ?: return@forEach, map["group"]?.toString())
                    }
                    else -> {
                        warning("Unrecognized control format: $map")
                        null
                    }
                }?.run {
                    control += this
                }
            }
        }
    }

    abstract class Control {

        abstract fun check(profile: PlayerProfile, template: Template): CompletableFuture<Boolean>

        abstract fun signature(profile: PlayerProfile, template: Template)
    }

    class ControlAgent(val agent: List<String>) : Control() {

        override fun check(profile: PlayerProfile, template: Template): CompletableFuture<Boolean> {
            return try {
                KetherShell.eval(agent.asList(), namespace = namespaceQuest) {
                    this.sender = profile.player
                    rootFrame().variables().also { vars ->
                        vars.set("@QuestContainer", template)
                    }
                }.thenApply {
                    Coerce.toBoolean(it)
                }
            } catch (e: Throwable) {
                e.print()
                CompletableFuture.completedFuture(false)
            }
        }

        override fun signature(profile: PlayerProfile, template: Template) {
        }
    }

    class ControlCooldown(val type: Trigger, val time: Time, val group: String?) : Control() {

        override fun check(profile: PlayerProfile, template: Template): CompletableFuture<Boolean> {
            val id = "quest.cooldown.${if (group != null) "@$group" else template.id}.${type.name.toLowerCase()}"
            val start = profile.persistentDataContainer[id, 0L].toLong()
            return CompletableFuture.completedFuture(time.`in`(start).isTimeout(start))
        }

        override fun signature(profile: PlayerProfile, template: Template) {
            val id = "quest.cooldown.${if (group != null) "@$group" else template.id}.${type.name.toLowerCase()}"
            profile.persistentDataContainer[id] = System.currentTimeMillis()
        }
    }

    class ControlCoexist(val alias: Int, val label: Map<String, Int>) : Control() {

        override fun check(profile: PlayerProfile, template: Template): CompletableFuture<Boolean> {
            if (alias > 0) {
                val a = template.alias()
                if (a != null && profile.getQuests().count { it.template.alias() == a } >= alias) {
                    return CompletableFuture.completedFuture(false)
                }
            }
            if (label.any { label -> profile.getQuests().count { label.key in it.template.label() } > label.value }) {
                return CompletableFuture.completedFuture(false)
            }
            return CompletableFuture.completedFuture(true)
        }

        override fun signature(profile: PlayerProfile, template: Template) {
        }
    }

    class ControlRepeat(val type: Trigger, val amount: Int, val period: Time?, val group: String?) : Control() {

        override fun check(profile: PlayerProfile, template: Template): CompletableFuture<Boolean> {
            val id = "quest.repeat.${if (group != null) "@$group" else template.id}.${type.name.toLowerCase()}"
            val time = profile.persistentDataContainer["$id.time", 0L].toLong()
            // 超出重复限时
            if (period != null && period.`in`(time).isTimeout(time)) {
                return CompletableFuture.completedFuture(true)
            }
            return CompletableFuture.completedFuture(profile.persistentDataContainer["$id.amount", 0].toInt() < amount)
        }

        override fun signature(profile: PlayerProfile, template: Template) {
            val id = "quest.repeat.${if (group != null) "@$group" else template.id}.${type.name.toLowerCase()}"
            val time = profile.persistentDataContainer["$id.time", 0L].toLong()
            // 超出重复限时
            if (period != null && period.`in`(time).isTimeout(time)) {
                // 初始化变量
                profile.persistentDataContainer["$id.amount"] = 1
                profile.persistentDataContainer["$id.time"] = System.currentTimeMillis()
            } else {
                // 追加次数
                profile.persistentDataContainer["$id.amount"] = profile.persistentDataContainer["$id.amount", 0].toInt() + 1
            }
        }
    }

    class ControlOperator(val template: Template, val control: List<Control>?) {

        fun check(profile: PlayerProfile): CompletableFuture<Boolean> {
            val future = CompletableFuture<Boolean>()
            if (control == null) {
                future.complete(true)
                return future
            }
            mirrorFuture("MetaControl:check") {
                fun process(cur: Int) {
                    if (cur < control.size) {
                        control[cur].check(profile, template).thenApply {
                            if (it) {
                                process(cur + 1)
                            } else {
                                future.complete(false)
                                finish()
                            }
                        }
                    } else {
                        future.complete(true)
                        finish()
                    }
                }
                process(0)
            }
            return future
        }

        fun signature(profile: PlayerProfile, type: Trigger = Trigger.COMPLETE) {
            control?.filter { it !is ControlRepeat || it.type == type }?.forEach { it.signature(profile, template) }
        }
    }

    enum class Trigger {

        ACCEPT, FAILURE, COMPLETE;

        companion object {

            fun fromName(name: String) = Enums.getIfPresent(Trigger::class.java, name.toUpperCase()).or(COMPLETE)!!
        }
    }

    companion object {

        fun Template.control() = ControlOperator(this, meta<MetaControl>("control")?.control)
    }
}