package ink.ptms.chemdah.module.ui

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Quest
import ink.ptms.chemdah.core.quest.addon.AddonUI.Companion.ui
import ink.ptms.chemdah.core.quest.meta.MetaName.Companion.displayName
import ink.ptms.chemdah.util.setIcon
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack

/**
 * Chemdah
 * ink.ptms.chemdah.module.ui.UIItemQuest
 *
 * @author sky
 * @since 2021/3/11 9:03 上午
 */
open class ItemQuest(config: ConfigurationSection) : Item(config) {

    override fun getItemStack(player: PlayerProfile, ui: UI, quest: Quest): ItemStack {
        return super.getItemStack(player, ui, quest).also { item ->
            val addonUI = quest.template.ui()
            if (addonUI?.icon != null) {
                item.setIcon(addonUI.icon)
            }
            item.itemMeta = item.itemMeta?.also { meta ->
                meta.setDisplayName(meta.displayName.replace("{name}", quest.template.displayName()))
                meta.lore = meta.lore?.flatMap { lore ->
                    if (lore.contains("{description}")) {
                        addonUI?.description?.map { lore.replace("{description}", it) } ?: emptyList()
                    } else {
                        listOf(lore)
                    }
                }
            }
        }
    }
}