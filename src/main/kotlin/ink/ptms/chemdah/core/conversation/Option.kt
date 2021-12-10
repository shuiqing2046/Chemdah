package ink.ptms.chemdah.core.conversation

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.core.conversation.theme.Theme
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.chat.colored
import taboolib.module.configuration.SecuredFile

/**
 * Chemdah
 * ink.ptms.chemdah.core.conversation.Option
 *
 * @author sky
 * @since 2021/2/10 10:22 上午
 */
data class Option(
    val root: ConfigurationSection,
    val theme: String = root.getString("theme", "chat")!!,
    val title: String = root.getString("title", "NPC")!!.colored()
) {

    val instanceTheme: Theme<*>
        get() = ChemdahAPI.getConversationTheme(theme) ?: error("theme $theme not supported.")

    val globalFlags = root.getStringList("global-flags")

    companion object {

        val default = Option(SecuredFile().createSection("__option__"))
    }
}