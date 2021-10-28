package ink.ptms.chemdah.core.quest.addon

import ink.ptms.chemdah.api.event.collect.TemplateEvents
import ink.ptms.chemdah.core.quest.Id
import ink.ptms.chemdah.core.quest.Option
import ink.ptms.chemdah.core.quest.Template
import ink.ptms.chemdah.core.quest.addon.data.*
import ink.ptms.chemdah.util.asInt
import ink.ptms.chemdah.util.asMap
import taboolib.common.platform.function.warning
import taboolib.common.util.asList
import taboolib.common5.util.parseTimeCycle

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.addon.AddonControl
 *
 * @author sky
 * @since 2021/3/1 11:47 下午
 */
@Id("control")
@Option(Option.Type.MAP_LIST)
class AddonControl(root: List<Map<String, Any>>, template: Template) : Addon(root, template) {

    val control = ArrayList<Control>()

    init {
        root.forEach { map ->
            if (map["$"] != null) {
                control += ControlAgent(map["$"]!!.asList())
            } else {
                val type = map["type"].toString().lowercase()
                when {
                    type == "coexist" -> {
                        ControlCoexist(map["amount"].asMap().map { it.key to it.value.asInt() }.toMap())
                    }
                    type.startsWith("repeat") -> {
                        val trigger = ControlTrigger.fromName(type.substring("repeat".length).trim())
                        ControlRepeat(trigger, map["amount"].asInt(), map["period"]?.toString()?.parseTimeCycle(), map["group"]?.toString())
                    }
                    type.startsWith("cooldown") -> {
                        val trigger = ControlTrigger.fromName(type.substring("cooldown".length).trim())
                        ControlCooldown(trigger, map["time"]?.toString()?.parseTimeCycle() ?: return@forEach, map["group"]?.toString())
                    }
                    else -> {
                        val event = TemplateEvents.ControlHook(questContainer as Template, type, map)
                        event.call()
                        if (event.control != null) {
                            warning("Unrecognized control format: $type $map")
                        }
                        event.control
                    }
                }?.run {
                    control += this
                }
            }
        }
    }

    companion object {

        fun Template.control() = ControlOperator(this, addon<AddonControl>("control")?.control)
    }
}