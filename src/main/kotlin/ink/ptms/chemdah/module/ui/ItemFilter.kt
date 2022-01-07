package ink.ptms.chemdah.module.ui

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Template
import ink.ptms.chemdah.util.replaces
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import taboolib.common.util.asList
import taboolib.library.configuration.ConfigurationSection
import taboolib.platform.util.modifyMeta

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
            item.modifyMeta<ItemMeta> {
                lore = lore?.flatMap { lore ->
                    if (lore.contains("filter")) {
                        val filters = ui.playerFilters.getOrDefault(player.uniqueId, arrayListOf(allKey)).toMutableList()
                        if (filters.isEmpty()) {
                            filters.add(allKey)
                        }
                        filters.map { lore.replaces("filter" to it) }
                    } else {
                        lore.asList()
                    }
                }
                addItemFlags(*ItemFlag.values())
            }
        }
    }
}