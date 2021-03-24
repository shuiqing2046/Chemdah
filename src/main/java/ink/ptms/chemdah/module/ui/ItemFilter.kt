package ink.ptms.chemdah.module.ui

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Quest
import ink.ptms.chemdah.util.asList
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack

/**
 * Chemdah
 * ink.ptms.chemdah.module.ui.UIItemFilter
 *
 * @author sky
 * @since 2021/3/11 9:03 上午
 */
open class ItemFilter(config: ConfigurationSection) : Item(config) {

    val allKey = config.getString("all-key")!!

    override fun getItemStack(player: PlayerProfile, ui: UI, quest: Quest): ItemStack {
        return super.getItemStack(player, ui, quest).also { item ->
            item.itemMeta = item.itemMeta?.also { meta ->
                meta.lore = meta.lore?.flatMap { lore ->
                    if (lore.contains("{filter}")) {
                        ui.playerFilters.getOrDefault(player.uniqueId, listOf(allKey)).map { lore.replace("{filter}", it) }
                    } else {
                        lore.asList()
                    }
                }
            }
        }
    }
}