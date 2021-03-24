package ink.ptms.chemdah.module.ui

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Quest
import io.izzel.taboolib.kotlin.kether.KetherFunction
import io.izzel.taboolib.util.item.Items
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack

/**
 * Chemdah
 * ink.ptms.chemdah.module.ui.UIItem
 *
 * @author sky
 * @since 2021/3/11 9:03 上午
 */
open class Item(val config: ConfigurationSection) {

    val itemStackBase = Items.loadItem(config)!!

    open fun getItemStack(player: PlayerProfile, ui: UI, quest: Quest): ItemStack {
        return itemStackBase.clone().also { item ->
            item.itemMeta = item.itemMeta?.also { meta ->
                meta.setDisplayName(KetherFunction.parse(meta.displayName))
                meta.lore = item.lore?.map { lore ->
                    KetherFunction.parse(lore)
                }
            }
        }
    }
}