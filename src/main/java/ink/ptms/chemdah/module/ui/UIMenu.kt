package ink.ptms.chemdah.module.ui

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Template
import ink.ptms.chemdah.core.quest.addon.AddonTrack.Companion.isTrackable
import ink.ptms.chemdah.core.quest.addon.AddonTrack.Companion.trackQuest
import io.izzel.taboolib.module.db.local.SecuredFile
import io.izzel.taboolib.util.item.Items
import io.izzel.taboolib.util.item.inventory.ClickEvent
import io.izzel.taboolib.util.item.inventory.linked.MenuLinked
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

/**
 * Chemdah
 * ink.ptms.chemdah.module.ui.UIMenu
 *
 * @author sky
 * @since 2021/3/25 7:18 下午
 */
class UIMenu(val ui: UI, val profile: PlayerProfile, val templates: List<UITemplate>) : MenuLinked<UITemplate>(profile.player) {

    init {
        addButton(ui.menuQuestSlotFilter) {
            UIMenuFilter(ui, profile).open()
        }
        addButton(-999) {
            when {
                it.castClick().isLeftClick -> {
                    if (hasPreviousPage()) {
                        open(page - 1)
                    }
                }
                it.castClick().isRightClick -> {
                    if (hasNextPage()) {
                        open(page + 1)
                    }
                }
            }
        }
    }

    override fun getTitle(): String {
        return ui.name.replace("{name}", profile.player.name).replace("{page}", (page + 1).toString())
    }

    override fun getRows(): Int {
        return ui.menuQuestRows
    }

    override fun getElements(): List<UITemplate> {
        return templates
    }

    override fun getSlots(): List<Int> {
        return ui.menuQuestSlot
    }

    override fun onBuild(inventory: Inventory) {
    }

    override fun onBuildAsync(inventory: Inventory) {
        inventory.setItem(ui.menuQuestSlotInfo, ui.items[ItemType.INFO]!!.getItemStack(profile, ui, unavailable))
        inventory.setItem(ui.menuQuestSlotFilter, ui.items[ItemType.FILTER]!!.getItemStack(profile, ui, unavailable))
        val item = ui.items[ItemType.QUEST_UNAVAILABLE]!!.getItemStack(profile, ui, unavailable)
        ui.menuQuestSlot.forEach {
            if (Items.isNull(inventory.getItem(it))) {
                inventory.setItem(it, item)
            }
        }
    }

    override fun onClick(event: ClickEvent, template: UITemplate) {
        // 当任务为正在进行或可以开始时
        if (template.itemType == ItemType.QUEST_STARTED || template.itemType == ItemType.QUEST_CAN_START) {
            // 当任务允许被追踪时才会关闭界面
            if (template.template.isTrackable()) {
                // 追踪任务
                profile.trackQuest = template.template
                // 播放音效并关闭界面
                event.clicker.playSound(event.clicker.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f)
                event.clicker.closeInventory()
            }
        }
    }

    override fun generateItem(player: Player, template: UITemplate, index: Int, slot: Int): ItemStack {
        return ui.items[template.itemType]!!.getItemStack(profile, ui, template.template)
    }

    companion object {

        private val unavailable = Template("__unavailable__", SecuredFile())
    }
}