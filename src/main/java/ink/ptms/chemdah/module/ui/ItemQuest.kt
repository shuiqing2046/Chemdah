package ink.ptms.chemdah.module.ui

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Template
import ink.ptms.chemdah.core.quest.addon.AddonUI.Companion.ui
import ink.ptms.chemdah.core.quest.meta.MetaName.Companion.displayName
import ink.ptms.chemdah.util.colored
import ink.ptms.chemdah.util.setIcon
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

/**
 * Chemdah
 * ink.ptms.chemdah.module.ui.ItemQuest
 *
 * @author sky
 * @since 2021/3/11 9:03 上午
 */
open class ItemQuest(config: ConfigurationSection) : Item(config) {

    override fun getItemStack(player: PlayerProfile, ui: UI, template: Template): ItemStack {
        return super.getItemStack(player, ui, template).also { item ->
            val addonUI = template.ui()
            if (addonUI?.icon != null) {
                item.setIcon(addonUI.icon)
            }
            item.itemMeta = item.itemMeta?.also { meta ->
                meta.setDisplayName(meta.displayName.replace("{name}", template.displayName()))
                meta.lore = meta.lore?.flatMap { lore ->
                    if (lore.contains("{description}")) {
                        addonUI?.description?.map { lore.replace("{description}", it.colored()) } ?: emptyList()
                    } else {
                        listOf(lore)
                    }
                }
                meta.addItemFlags(*ItemFlag.values())
            }
        }
    }
}