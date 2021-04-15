package ink.ptms.chemdah.core.conversation.theme.chest

import ink.ptms.chemdah.util.colored
import io.izzel.taboolib.util.item.ItemBuilder
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack

class ThemeChestSetting(
    val root: ConfigurationSection,
    val title: String = root.getString("title")!!,
    val npcIcon: ConfigurationSection = root.getConfigurationSection("npc-icon")!!,
    val replyIcon: ConfigurationSection = root.getConfigurationSection("reply-icon")!!,
    val npcSlot: Int = root.getInt("layout.npc-slot"),
    val replySlots: List<Int> = root.getIntegerList("layout.reply-slot"),
) {


}