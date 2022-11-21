package ink.ptms.chemdah.core.conversation.theme

import ink.ptms.chemdah.util.mapSection
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import taboolib.library.configuration.ConfigurationSection
import taboolib.library.xseries.XItemStack
import taboolib.library.xseries.getItemStack
import taboolib.module.chat.colored

/**
 * Chemdah
 * ink.ptms.chemdah.core.conversation.ThemeChestSetting
 *
 * @author sky
 * @since 2021/2/12 2:08 上午
 */
class ThemeChestSetting(root: ConfigurationSection) : ThemeSettings(root) {

    /** 标题 **/
    val title = root.getString("ui.title", "")!!.colored()

    /** 行数 **/
    val rows = root.getString("ui.rows", "1")!!

    /** NPC 物品 **/
    val npcItem = root.getItemStack("npc-side.item") ?: ItemStack(Material.STONE)
        get() = field.clone()

    /** 玩家物品 **/
    val playerItem = root.getItemStack("player-side.item") ?: ItemStack(Material.STONE)
        get() = field.clone()

    /** 曾被选过的玩家物品 **/
    val playerItemSelected = root.getItemStack("player-side.item-selected") ?: ItemStack(Material.STONE)
        get() = field.clone()

    /** 自定义玩家物品 **/
    private val playerItemCustom = root.mapSection("player-side.item-custom") { XItemStack.deserialize(it) { i -> i.colored() } }

    /** NPC 物品位置 **/
    val npcSlot = root.getInt("npc-side.slot")

    /** 玩家物品位置 **/
    val playerSlot = root.getIntegerList("player-side.slot")

    /** 获取自定义玩家物品 **/
    fun getCustomPlayerItem(name: String): ItemStack? {
        return playerItemCustom[name]?.clone()
    }
}