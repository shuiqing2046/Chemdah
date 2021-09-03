package ink.ptms.chemdah.module.ui

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Template
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import taboolib.common.util.asList
import taboolib.library.configuration.ConfigurationSection

/**
 * Chemdah
 * ink.ptms.chemdah.module.ui.ItemFilter
 *
 * @author sky
 * @since 2021/3/11 9:03 上午
 */
open class ItemFilter(config: ConfigurationSection) : Item(config) {

    val allKey = config.getString("all-key")!!

    override fun getItemStack(player: PlayerProfile, ui: UI, template: Template): ItemStack {
        return super.getItemStack(player, ui, template).also { item ->
            item.itemMeta = item.itemMeta?.also { meta ->
                meta.lore = meta.lore?.flatMap { lore ->
                    if (lore.contains("{filter}")) {
                        val filters = ui.playerFilters.getOrDefault(player.uniqueId, arrayListOf(allKey)).toMutableList()
                        if (filters.isEmpty()) {
                            filters.add(allKey)
                        }
                        filters.map { lore.replace("{filter}", it) }
                    } else {
                        lore.asList()
                    }
                }
                meta.addItemFlags(*ItemFlag.values())
            }
        }
    }
}