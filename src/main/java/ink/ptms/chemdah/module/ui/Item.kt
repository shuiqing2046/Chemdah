package ink.ptms.chemdah.module.ui

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Template
import ink.ptms.chemdah.util.namespaceQuestUI
import io.izzel.taboolib.kotlin.kether.KetherFunction
import io.izzel.taboolib.util.item.Items
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack

/**
 * Chemdah
 * ink.ptms.chemdah.module.ui.Item
 *
 * @author sky
 * @since 2021/3/11 9:03 上午
 */
open class Item(val config: ConfigurationSection) {

    val itemStackBase = Items.loadItem(config)!!

    open fun getItemStack(player: PlayerProfile, ui: UI, template: Template): ItemStack {
        return itemStackBase.clone().also { item ->
            item.itemMeta = item.itemMeta?.also { meta ->
                meta.setDisplayName(KetherFunction.parse(meta.displayName) {
                    sender = player.player
                    rootFrame().variables().set("@QuestUI", ui)
                })
                meta.lore = meta.lore?.map { lore ->
                    KetherFunction.parse(lore, namespace = namespaceQuestUI) {
                        sender = player.player
                        rootFrame().variables().set("@QuestUI", ui)
                    }
                }
            }
        }
    }
}