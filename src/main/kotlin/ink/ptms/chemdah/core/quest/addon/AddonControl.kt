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

    /** 管控列表 */
    val control = root.mapNotNull { map ->
        // 脚本代理
        if (map["$"] != null) {
            ControlAgent(map["$"]!!.asList())
        } else {
            // 获取管控类型
            val type = map["type"].toString().lowercase()
            when {
                // 共存
                type == "coexist" -> {
                    ControlCoexist(map["amount"].asMap().map { it.key to it.value.asInt() }.toMap())
                }
                // 重复
                type.startsWith("repeat") -> {
                    val trigger = ControlTrigger.fromName(type.substring("repeat".length).trim())
                    ControlRepeat(trigger, map["amount"].asInt(), map["period"]?.toString()?.parseTimeCycle(), map["group"]?.toString())
                }
                // 冷却
                type.startsWith("cooldown") -> {
                    val trigger = ControlTrigger.fromName(type.substring("cooldown".length).trim())
                    ControlCooldown(trigger, map["time"]?.toString()?.parseTimeCycle() ?: return@mapNotNull null, map["group"]?.toString())
                }
                // 第三方支持
                else -> {
                    val event = TemplateEvents.ControlHook(questContainer as Template, type, map)
                    event.call()
                    if (event.control != null) {
                        warning("Unrecognized control format: $type $map")
                    }
                    event.control
                }
            }
        }
    }.toMutableList()

    companion object {

        /** 获取管控组件 */
        fun Template.control() = ControlOperator(this, addon<AddonControl>("control")?.control)
    }
}