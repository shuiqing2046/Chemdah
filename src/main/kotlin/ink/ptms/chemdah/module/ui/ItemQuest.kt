package ink.ptms.chemdah.module.ui

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Template
import ink.ptms.chemdah.core.quest.addon.AddonUI.Companion.ui
import ink.ptms.chemdah.util.setIcon
import org.bukkit.inventory.ItemStack
import taboolib.library.configuration.ConfigurationSection

/**
 * Chemdah
 * ink.ptms.chemdah.module.ui.ItemQuest
 *
 * @author sky
 * @since 2021/3/11 9:03 上午
 */
open class ItemQuest(config: ConfigurationSection) : ItemQuestNoIcon(config) {

    override fun getItemStack(player: PlayerProfile, ui: UI, template: Template): ItemStack {
        return super.getItemStack(player, ui, template).also { item ->
            val addonUI = template.ui()
            if (addonUI?.icon != null) {
                item.setIcon(addonUI.icon)
            }
        }
    }
}