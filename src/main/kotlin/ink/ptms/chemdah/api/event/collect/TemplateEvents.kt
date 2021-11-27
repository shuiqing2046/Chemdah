package ink.ptms.chemdah.api.event.collect

import ink.ptms.chemdah.core.conversation.Option
import ink.ptms.chemdah.core.quest.Template
import ink.ptms.chemdah.core.quest.addon.data.Control
import taboolib.library.configuration.ConfigurationSection
import taboolib.platform.type.BukkitProxyEvent
import java.io.File

/**
 * Chemdah
 * ink.ptms.chemdah.api.event.collect.TemplateEvents
 *
 * @author sky
 * @since 2021/5/7 12:13 上午
 */
class TemplateEvents {

    /**
     * 当模板被加载
     */
    class Load(val file: File?, val id: String, val root: ConfigurationSection): BukkitProxyEvent()

    class ControlHook(val template: Template, val type: String, val map: Map<String, Any>) : BukkitProxyEvent() {

        override val allowCancelled: Boolean
            get() = false

        var control: Control? = null
    }
}