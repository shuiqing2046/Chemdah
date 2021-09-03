package ink.ptms.chemdah.module.ui

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Template
import ink.ptms.chemdah.util.namespaceQuestUI
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.function.adaptCommandSender
import taboolib.library.configuration.ConfigurationSection
import taboolib.library.xseries.XItemStack
import taboolib.module.kether.KetherFunction

/**
 * Chemdah
 * ink.ptms.chemdah.module.ui.Item
 *
 * @author sky
 * @since 2021/3/11 9:03 上午
 */
open class Item(val config: ConfigurationSection) {

    val itemStackBase = XItemStack.deserialize(config)!!

    open fun getItemStack(player: PlayerProfile, ui: UI, template: Template): ItemStack {
        return itemStackBase.clone().also { item ->
            item.itemMeta = item.itemMeta?.also { meta ->
                meta.setDisplayName(KetherFunction.parse(meta.displayName, sender = adaptCommandSender(player.player)) {
                    rootFrame().variables().set("@QuestUI", ui)
                })
                meta.lore = meta.lore?.map { lore ->
                    KetherFunction.parse(lore, namespace = namespaceQuestUI, sender = adaptCommandSender(player.player)) {
                        rootFrame().variables().set("@QuestUI", ui)
                    }
                }
            }
        }
    }
}