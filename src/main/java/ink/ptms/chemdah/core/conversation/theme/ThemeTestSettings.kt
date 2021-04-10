package ink.ptms.chemdah.core.conversation.theme

import ink.ptms.chemdah.util.colored
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
    val format: List<String> = root.getStringList("format").map { it.colored() },
    val selectChar: String = root.getString("select.char", "")!!,
    val selectOther: String = root.getString("select.other", "")!!,
    val selectColor: String = root.getString("select.color", "")!!.colored(),
    val talking: String = root.getString("talking", "")!!.colored(),
    val animation: Boolean = root.getBoolean("animation", true)
)