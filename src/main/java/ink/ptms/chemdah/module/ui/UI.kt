package ink.ptms.chemdah.module.ui

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.util.colored
import io.izzel.taboolib.util.item.Items
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * Chemdah
 * ink.ptms.chemdah.module.ui.UI
 *
 * @author sky
 * @since 2021/3/11 9:03 上午
 */
class UI(val config: ConfigurationSection) {

    val name = config.getString("name")?.colored().toString()
    val include: List<String> = config.getStringList("include")
    val exclude: List<String> = config.getStringList("exclude")
    val items = HashMap<ItemType, Item>()

    init {

    }

    open class Item(val config: ConfigurationSection) {

        val itemStackBase = Items.loadItem(config)!!

        open fun getItemStack(player: PlayerProfile): ItemStack {
            return itemStackBase.clone()
        }
    }

    open class ItemInfo(config: ConfigurationSection): Item(config) {

        override fun getItemStack(player: PlayerProfile): ItemStack {
            return super.getItemStack(player)
        }
    }

    enum class ItemType {

        INFO,
        FILTER,
        QUEST_STARTED,
        QUEST_CAN_START,
        QUEST_CANNOT_START,
        QUEST_COMPLETE,
        QUEST_UNAVAILABLE
    }
}