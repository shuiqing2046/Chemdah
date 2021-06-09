package ink.ptms.chemdah.core.quest.meta

import com.google.common.base.Enums
import ink.ptms.chemdah.api.event.collect.TemplateEvents
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
 * ink.ptms.chemdah.core.quest.meta.MetaControl
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
                        val control = TemplateEvents.ControlHook(questContainer as Template, type, map).call().control
                        if (control != null) {
                            warning("Unrecognized control format: $type $map")
                        }
                        control
                    }
                }?.run {
                    control += this
                }
            }
        }
    }

    data class Result(val pass: Boolean, val reason: String? = null)

    abstract class Control {

        abstract val trigger: Trigger?

        abstract fun check(profile: PlayerProfile, template: Template): CompletableFuture<Result>

        abstract fun signature(profile: PlayerProfile, template: Template)

        fun Boolean.toResult(reason: String) = Result(this, reason)
    }

    class ControlAgent(val agent: List<String>) : Control() {

        override val trigger: Trigger?
            get() = null

        override fun check(profile: PlayerProfile, template: Template): CompletableFuture<Result> {
            return try {
                KetherShell.eval(agent.asList(), namespace = namespaceQuest) {
                    this.sender = profile.player
                    rootFrame().variables().also { vars ->
                        vars.set("@QuestContainer", template)
                    }
                }.thenApply {
                    Coerce.toBoolean(it).toResult("agent")
                }
            } catch (e: Throwable) {
                e.print()
                CompletableFuture.completedFuture(false.toResult("agent"))
            }
        }

        override fun signature(profile: PlayerProfile, template: Template) {
        }
    }

    class ControlCooldown(val type: Trigger, val time: Time, val group: String?) : Control() {

        override val trigger: Trigger
            get() = type

        override fun check(profile: PlayerProfile, template: Template): CompletableFuture<Result> {
            val id = "quest.cooldown.${if (group != null) "@$group" else template.id}.${type.name.toLowerCase()}"
            val start = profile.persistentDataContainer[id, 0L].toLong()
            return CompletableFuture.completedFuture(time.`in`(start).isTimeout(start).toResult("cooldown"))
        }

        override fun signature(profile: PlayerProfile, template: Template) {
            val id = "quest.cooldown.${if (group != null) "@$group" else template.id}.${type.name.toLowerCase()}"
            profile.persistentDataContainer[id] = System.currentTimeMillis()
        }
    }

    class ControlCoexist(val alias: Int, val label: Map<String, Int>) : Control() {

        override val trigger: Trigger?
            get() = null

        override fun check(profile: PlayerProfile, template: Template): CompletableFuture<Result> {
            if (alias > 0) {
                val a = template.alias()
                if (a != null && profile.getQuests().count { it.template.alias() == a } >= alias) {
                    return CompletableFuture.completedFuture(false.toResult("coexist"))
                }
            }
            if (label.any { label -> profile.getQuests().count { label.key in it.template.label() } > label.value }) {
                return CompletableFuture.completedFuture(false.toResult("coexist"))
            }
            return CompletableFuture.completedFuture(true.toResult("coexist"))
        }

        override fun signature(profile: PlayerProfile, template: Template) {
        }
    }

    class ControlRepeat(val type: Trigger, val amount: Int, val period: Time?, val group: String?) : Control() {

        override val trigger: Trigger
            get() = type

        override fun check(profile: PlayerProfile, template: Template): CompletableFuture<Result> {
            val id = "quest.repeat.${if (group != null) "@$group" else template.id}.${type.name.toLowerCase()}"
            val time = profile.persistentDataContainer["$id.time", 0L].toLong()
            // 超出重复限时
            if (period != null && period.`in`(time).isTimeout(time)) {
                return CompletableFuture.completedFuture(true.toResult("repeat"))
            }
            return CompletableFuture.completedFuture((profile.persistentDataContainer["$id.amount", 0].toInt() < amount).toResult("repeat"))
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

        /**
         * 任务是否被限制接受
         */
        fun check(profile: PlayerProfile): CompletableFuture<Result> {
            val future = CompletableFuture<Result>()
            if (control == null) {
                future.complete(Result(true))
                return future
            }
            mirrorFuture("MetaControl:check") {
                fun process(cur: Int) {
                    if (cur < control.size) {
                        control[cur].check(profile, template).thenApply {
                            if (it.pass) {
                                process(cur + 1)
                            } else {
                                future.complete(it)
                                finish()
                            }
                        }
                    } else {
                        future.complete(Result(true))
                        finish()
                    }
                }
                process(0)
            }
            return future
        }

        fun signature(profile: PlayerProfile, type: Trigger = Trigger.COMPLETE) {
            control?.filter { it.trigger == null || it.trigger == type }?.forEach { it.signature(profile, template) }
        }
    }

    enum class Trigger {

        ACCEPT, FAIL, COMPLETE;

        companion object {

            fun fromName(name: String) = Enums.getIfPresent(Trigger::class.java, name.toUpperCase()).or(COMPLETE)!!
        }
    }

    companion object {

        fun Template.control() = ControlOperator(this, meta<MetaControl>("control")?.control)
    }
}