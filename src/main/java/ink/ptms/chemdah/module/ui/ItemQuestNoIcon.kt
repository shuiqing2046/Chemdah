package ink.ptms.chemdah.module.ui

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Template
import ink.ptms.chemdah.core.quest.addon.AddonUI.Companion.ui
import ink.ptms.chemdah.core.quest.meta.MetaName.Companion.displayName
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack

/**
 * Chemdah
 * ink.ptms.chemdah.module.ui.ItemQuestNoIcon
 *
 * @author sky
 * @since 2021/3/11 9:03 上午
 */
open class ItemQuestNoIcon(config: ConfigurationSection) : Item(config) {

    override fun getItemStack(player: PlayerProfile, ui: UI, template: Template): ItemStack {
        return super.getItemStack(player, ui, template).also { item ->
            item.itemMeta = item.itemMeta?.also { meta ->
                meta.setDisplayName(meta.displayName.replace("{name}", template.displayName()))
                meta.lore = meta.lore?.flatMap { lore ->
                    if (lore.contains("{description}")) {
                        template.ui()?.description?.map { lore.replace("{description}", it) } ?: emptyList()
                    } else {
                        listOf(lore)
                    }
                }
            }
        }
    }
}