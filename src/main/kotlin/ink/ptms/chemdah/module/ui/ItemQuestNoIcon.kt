package ink.ptms.chemdah.module.ui

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Template
import ink.ptms.chemdah.core.quest.addon.AddonUI.Companion.ui
import ink.ptms.chemdah.core.quest.meta.MetaName.Companion.displayName
import ink.ptms.chemdah.util.replace
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import taboolib.library.configuration.ConfigurationSection
import taboolib.platform.util.modifyMeta

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
            val addonUI = template.ui()
            item.modifyMeta<ItemMeta> {
                setDisplayName(displayName.replace("name" to format(template.displayName(colored = false), player, ui, template)))
                lore = lore?.flatMap { lore ->
                    if (lore.contains("description")) {
                        addonUI?.description?.map { lore.replace("description" to format(it, player, ui, template)) } ?: emptyList()
                    } else {
                        listOf(lore)
                    }
                }
                addItemFlags(*ItemFlag.values())
            }
        }
    }
}