package ink.ptms.chemdah.module.ui

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Quest
import ink.ptms.chemdah.core.quest.addon.AddonUI.Companion.ui
import ink.ptms.chemdah.core.quest.meta.MetaName.Companion.displayName
import ink.ptms.chemdah.util.colored
import ink.ptms.chemdah.util.setIcon
import io.izzel.taboolib.kotlin.kether.KetherFunction
import io.izzel.taboolib.util.item.Items
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack
import java.util.*
import kotlin.collections.HashMap

/**
 * Chemdah
 * ink.ptms.chemdah.module.ui.UI
 *
 * @author sky
 * @since 2021/3/11 9:03 上午
 */
class UI(val config: ConfigurationSection) {

    val playerFilters = HashMap<UUID, List<String>>()

    val name = config.getString("name")?.colored().toString()
    val include: List<String> = config.getStringList("include")
    val exclude: List<String> = config.getStringList("exclude")
    val items = HashMap<ItemType, Item>()

    init {
        items[ItemType.INFO] = Item(config.getConfigurationSection("item.info")!!)
        items[ItemType.FILTER] = ItemFilter(config.getConfigurationSection("item.filter")!!)
        items[ItemType.QUEST_STARTED] = Item(config.getConfigurationSection("item.quest.started")!!)
        items[ItemType.QUEST_CAN_START] = Item(config.getConfigurationSection("item.quest.can-start")!!)
        items[ItemType.QUEST_CANNOT_START] = Item(config.getConfigurationSection("item.quest.cannot-start")!!)
        items[ItemType.QUEST_COMPLETE] = Item(config.getConfigurationSection("item.quest.completed")!!)
        items[ItemType.QUEST_UNAVAILABLE] = Item(config.getConfigurationSection("item.quest.unavailable")!!)
    }

    open class Item(val config: ConfigurationSection) {

        val itemStackBase = Items.loadItem(config)!!

        open fun getItemStack(player: PlayerProfile, ui: UI, quest: Quest): ItemStack {
            return itemStackBase.clone().also {
                it.itemMeta = it.itemMeta?.also { meta ->
                    meta.setDisplayName(KetherFunction.parse(meta.displayName))
                    meta.lore = it.lore?.map { lore ->
                        KetherFunction.parse(lore)
                    }
                }
            }
        }
    }

    open class ItemFilter(config: ConfigurationSection) : Item(config) {

        val allKey = config.getString("all-key")!!

        override fun getItemStack(player: PlayerProfile, ui: UI, quest: Quest): ItemStack {
            return super.getItemStack(player, ui, quest).also {
                it.itemMeta = it.itemMeta?.also { meta ->
                    meta.lore = meta.lore?.flatMap { lore ->
                        if (lore.contains("{filter}")) {
                            val filter = ui.playerFilters[player.player.uniqueId] ?: listOf(allKey)
                            filter.map { i -> lore.replace("{filter}", i) }
                        } else {
                            listOf(lore)
                        }
                    }
                }
            }
        }
    }

    open class ItemQuest(config: ConfigurationSection) : Item(config) {

        override fun getItemStack(player: PlayerProfile, ui: UI, quest: Quest): ItemStack {
            return super.getItemStack(player, ui, quest).also {
                val addonUI = quest.template.ui()
                if (addonUI?.icon != null) {
                    it.setIcon(addonUI.icon)
                }
                it.itemMeta = it.itemMeta?.also { meta ->
                    meta.setDisplayName(meta.displayName.replace("{name}", quest.template.displayName()))
                    meta.lore = meta.lore?.flatMap { lore ->
                        if (lore.contains("{description}")) {
                            addonUI?.description?.map { i -> lore.replace("{description}", i) } ?: emptyList()
                        } else {
                            listOf(lore)
                        }
                    }
                }
            }
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