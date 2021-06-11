package ink.ptms.chemdah.api.event.collect

import ink.ptms.chemdah.core.quest.Template
import ink.ptms.chemdah.core.quest.addon.AddonControl
import io.izzel.taboolib.module.event.EventNormal

/**
 * Chemdah
 * ink.ptms.chemdah.api.event.collect.TemplateEvents
 *
 * @author sky
 * @since 2021/5/7 12:13 上午
 */
class TemplateEvents {

    class ControlHook(val template: Template, val type: String, val map: Map<String, Any>): EventNormal<ControlHook>(true) {

        var control: AddonControl.Control? = null
    }
}