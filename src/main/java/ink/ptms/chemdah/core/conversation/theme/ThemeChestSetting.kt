package ink.ptms.chemdah.core.conversation.theme

import ink.ptms.chemdah.util.colored
import io.izzel.taboolib.module.db.local.SecuredFile
import io.izzel.taboolib.util.Features
import io.izzel.taboolib.util.item.Items
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack
import javax.script.CompiledScript

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
    val rows: CompiledScript? = Features.compileScript(root.getString("ui.rows", "1")!!),
    npcItem: ItemStack = Items.loadItem(root.getConfigurationSection("npc-side.item") ?: EMPTY_SECTION)!!,
    playerItem: ItemStack = Items.loadItem(root.getConfigurationSection("player-side.item") ?: EMPTY_SECTION)!!,
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

        val EMPTY_SECTION = SecuredFile().createSection("__empty__").also {
            it.set("material", "stone")
        }
    }
}