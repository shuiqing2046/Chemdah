package ink.ptms.chemdah.core.conversation.theme.chest

import org.bukkit.configuration.ConfigurationSection

class ThemeChestSetting(
    val root: ConfigurationSection,
    val title: ConfigurationSection = root.getConfigurationSection("title")!!,
    val layoutRowAmount: Int = root.getInt("layout.amount-amount"),
    val layoutList: List<String> = root.getStringList("layout.list")
) {

    override fun toString(): String {
        return "ThemeChestSetting(root=$root, title=$title, layoutRowAmount=$layoutRowAmount, layoutList=$layoutList)"
    }
}