package ink.ptms.chemdah.core.conversation

import org.bukkit.configuration.ConfigurationSection

/**
 * Chemdah
 * ink.ptms.chemdah.core.conversation.FormatTest
 *
 * @author sky
 * @since 2021/2/12 2:08 上午
 */
data class ThemeTestSettings(
    val root: ConfigurationSection,
    val format: List<String>,
    val selectChar: String,
    val selectOther: String,
    val selectColor: String
) {


}