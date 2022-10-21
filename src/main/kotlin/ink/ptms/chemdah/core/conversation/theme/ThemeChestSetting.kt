package ink.ptms.chemdah.core.conversation.theme

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import taboolib.library.configuration.ConfigurationSection
import taboolib.library.xseries.XItemStack
import taboolib.library.xseries.getItemStack
import taboolib.module.chat.colored
import taboolib.module.configuration.SecuredFile

/**
 * Chemdah
 * ink.ptms.chemdah.core.conversation.ThemeChestSetting
 *
 * @author sky
 * @since 2021/2/12 2:08 上午
 */
class ThemeChestSetting(root: ConfigurationSection) : ThemeSettings(root) {

    val title = root.getString("ui.title", "")!!.colored()
    val rows = root.getString("ui.rows", "1")!!

    val npcItem = root.getItemStack("npc-side.item") ?: ItemStack(Material.STONE)
        get() = field.clone()

    val playerItem = root.getItemStack("player-side.item") ?: ItemStack(Material.STONE)
        get() = field.clone()

    val playerItemSelected = root.getItemStack("player-side.item-selected") ?: ItemStack(Material.STONE)
        get() = field.clone()

    val npcSlot = root.getInt("npc-side.slot")
    val playerSlot = root.getIntegerList("player-side.slot")

    override fun toString(): String {
        return "ThemeChestSetting(title='$title', rows='$rows', npcItem=$npcItem, playerItem=$playerItem, playerItemSelected=$playerItemSelected, npcSlot=$npcSlot, playerSlot=$playerSlot)"
    }
}