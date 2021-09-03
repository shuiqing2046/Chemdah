package ink.ptms.chemdah.core.conversation.theme

import org.bukkit.inventory.ItemStack
import taboolib.library.configuration.ConfigurationSection
import taboolib.library.xseries.XItemStack
import taboolib.module.chat.colored
import taboolib.module.configuration.SecuredFile

/**
 * Chemdah
 * ink.ptms.chemdah.core.conversation.ThemeChestSetting
 *
 * @author sky
 * @since 2021/2/12 2:08 上午
 */
class ThemeChestSetting(
    root: ConfigurationSection,
    val title: String = root.getString("ui.title", "")!!.colored(),
    val rows: String = root.getString("ui.rows", "*1")!!,
    npcItem: ItemStack = XItemStack.deserialize(root.getConfigurationSection("npc-side.item") ?: EMPTY_SECTION)!!,
    playerItem: ItemStack = XItemStack.deserialize(root.getConfigurationSection("player-side.item") ?: EMPTY_SECTION)!!,
    val npcSlot: Int = root.getInt("npc-side.slot"),
    val playerSlot: List<Int> = root.getIntegerList("player-side.slot"),
) : ThemeSettings(root) {

    val npcItem = npcItem
        get() = field.clone()

    val playerItem = playerItem
        get() = field.clone()

    override fun toString(): String {
        return "ThemeChestSetting(" +
                "title='$title', " +
                "rows=$rows, " +
                "npcSlot=$npcSlot, " +
                "playerSlot=$playerSlot, " +
                "npcItem=$npcItem, " +
                "playerItem=$playerItem" +
                ")"
    }

    companion object {

        val EMPTY_SECTION: ConfigurationSection = SecuredFile().createSection("__empty__").also {
            it.set("material", "stone")
        }
    }
}