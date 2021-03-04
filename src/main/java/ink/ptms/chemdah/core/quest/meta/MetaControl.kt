package ink.ptms.chemdah.core.quest.meta

import com.google.common.base.Enums
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Id
import ink.ptms.chemdah.core.quest.QuestContainer
import ink.ptms.chemdah.core.quest.Template
import ink.ptms.chemdah.core.quest.meta.MetaAlias.Companion.alias
import ink.ptms.chemdah.core.quest.meta.MetaLabel.Companion.label
import ink.ptms.chemdah.util.toTime
import ink.ptms.chemdah.util.warning
import io.izzel.taboolib.cronus.util.Time
import io.izzel.taboolib.util.Coerce

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.meta.MetaAliases
 *
 * @author sky
 * @since 2021/3/1 11:47 下午
 */
@Id("control")
@MetaType(MetaType.Type.MAP_LIST)
class MetaControl(source: List<Map<String, Any>>, questContainer: QuestContainer) : Meta<List<Map<String, Any>>>(source, questContainer) {

    val control = ArrayList<Control>()

    init {
        source.forEach { map ->
            val type = map["type"].toString().toLowerCase()
            when {
                type == "cooldown" -> {
                    ControlCooldown(map["time"]?.toString()?.toTime() ?: return@forEach, map["group"]?.toString())
                }
                type == "coexist" -> {
                    ControlCoexist(Coerce.toInteger(map["alias"]), map
                        .filterKeys {
                            it.startsWith("label(") && it.endsWith(")")
                        }.map {
                            it.key.substring("label(".length, it.key.length - 1) to Coerce.toInteger(it.value)
                        }.toMap()
                    )
                }
                type.startsWith("repeat") -> {
                    val trigger = ControlRepeat.Type.fromName(type.substring("repeat".length).trim())
                    ControlRepeat(trigger, Coerce.toInteger(map["amount"]), map["period"]?.toString()?.toTime(), map["group"]?.toString())
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

    abstract class Control {

        abstract fun check(profile: PlayerProfile, template: Template): Boolean

        abstract fun signature(profile: PlayerProfile, template: Template)
    }

    class ControlCooldown(val time: Time, val group: String?) : Control() {

        override fun check(profile: PlayerProfile, template: Template): Boolean {
            val id = "quest.cooldown.${if (group != null) "@$group" else template.id}"
            val start = profile.persistentDataContainer[id, 0L].toLong()
            return time.`in`(start).isTimeout(start)
        }

        override fun signature(profile: PlayerProfile, template: Template) {
            val id = "quest.cooldown.${if (group != null) "@$group" else template.id}"
            profile.persistentDataContainer.put(id, System.currentTimeMillis())
        }
    }

    class ControlCoexist(val alias: Int, val label: Map<String, Int>) : Control() {

        override fun check(profile: PlayerProfile, template: Template): Boolean {
            if (alias > 0) {
                val a = template.alias()
                if (a != null && profile.questValid.count { it.value.template.alias() == a } > alias) {
                    return false
                }
            }
            if (label.any { label -> profile.questValid.count { label.key in it.value.template.label() } > label.value }) {
                return false
            }
            return true
        }

        override fun signature(profile: PlayerProfile, template: Template) {
        }
    }

    class ControlRepeat(val type: Type, val amount: Int, val period: Time?, val group: String?) : Control() {

        override fun check(profile: PlayerProfile, template: Template): Boolean {
            val id = "quest.repeat.${if (group != null) "@$group" else template.id}.${type.name.toLowerCase()}"
            val time = profile.persistentDataContainer["$id.time", 0L].toLong()
            // 超出重复限时
            if (period != null && period.`in`(time).isTimeout(time)) {
                return true
            }
            return profile.persistentDataContainer["$id.amount", 0].toInt() < amount
        }

        override fun signature(profile: PlayerProfile, template: Template) {
            val id = "quest.repeat.${if (group != null) "@$group" else template.id}.${type.name.toLowerCase()}"
            val time = profile.persistentDataContainer["$id.time", 0L].toLong()
            // 超出重复限时
            if (period != null && period.`in`(time).isTimeout(time)) {
                // 初始化变量
                profile.persistentDataContainer.put("$id.amount", 1)
                profile.persistentDataContainer.put("$id.time", System.currentTimeMillis())
            } else {
                // 追加次数
                profile.persistentDataContainer.put("$id.amount", profile.persistentDataContainer["$id.amount", 0].toInt() + 1)
            }
        }

        enum class Type {

            ACCEPT, FAILURE, COMPLETE;

            companion object {

                fun fromName(name: String) = Enums.getIfPresent(Type::class.java, name.toUpperCase()).or(COMPLETE)!!
            }
        }
    }

    class ControlOperator(val template: Template, val control: List<Control>?) {

        fun check(profile: PlayerProfile): Boolean {
            return control == null || control.all { it.check(profile, template) }
        }

        fun signature(profile: PlayerProfile) {
            control?.forEach { it.signature(profile, template) }
        }
    }

    companion object {

        fun Template.control() = ControlOperator(this, meta<MetaControl>("control")?.control)
    }
}