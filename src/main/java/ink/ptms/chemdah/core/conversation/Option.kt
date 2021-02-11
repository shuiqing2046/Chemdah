package ink.ptms.chemdah.core.conversation

import ink.ptms.chemdah.api.ChemdahAPI
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration

/**
 * Chemdah
 * ink.ptms.chemdah.core.conversation.Option
 *
 * @author sky
 * @since 2021/2/10 10:22 上午
 */
data class Option(
    val root: ConfigurationSection,
    val theme: String = root.getString("theme", "test")!!,
    val title: String = root.getString("title", "NPC")!!
) {

    val instanceTheme: Theme
        get() = ChemdahAPI.getConversationTheme(theme) ?: ThemeTest

    companion object {

        val default = Option(YamlConfiguration().createSection("__option__"))
    }
}