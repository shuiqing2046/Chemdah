package ink.ptms.chemdah.module.ui

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Template
import ink.ptms.chemdah.util.namespaceQuestUI
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import taboolib.common.platform.function.adaptCommandSender
import taboolib.library.configuration.ConfigurationSection
import taboolib.library.xseries.XItemStack
import taboolib.module.chat.colored
import taboolib.module.kether.KetherFunction
import taboolib.module.kether.KetherShell
import taboolib.platform.util.modifyMeta

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
            item.modifyMeta<ItemMeta> {
                setDisplayName(format(displayName, player, ui, template))
                lore = lore?.map { lore -> format(lore, player, ui, template) }
            }
        }
    }

    /**
     * 进行 Kether 格式化并进行颜色替换
     */
    protected fun format(str: String, player: PlayerProfile, ui: UI, template: Template): String {
        return KetherFunction.parse(str,
            namespace = namespaceQuestUI,
            sender = adaptCommandSender(player.player),
            vars = KetherShell.VariableMap("@QuestUI" to ui, "@QuestSelected" to template.node)
        ).colored()
    }
}