package ink.ptms.chemdah.module.ui

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Template
import ink.ptms.chemdah.core.quest.addon.AddonTrack.Companion.allowTracked
import ink.ptms.chemdah.core.quest.addon.AddonTrack.Companion.trackQuest
import ink.ptms.chemdah.util.replace
import org.bukkit.Sound
import taboolib.module.configuration.Configuration
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Linked
import taboolib.platform.util.isAir

/**
 * Chemdah
 * ink.ptms.chemdah.module.ui.UIMenu
 *
 * @author sky
 * @since 2021/3/25 7:18 下午
 */
@Suppress("DuplicatedCode")
class UIMenu(val ui: UI, val profile: PlayerProfile, val templates: List<UITemplate>) {

    fun open(page: Int = 0) {
        profile.player.openMenu<Linked<UITemplate>>(ui.name.replace("name" to profile.player.name, "page" to "%p")) {
            page(page)
            rows(ui.menuQuestRows)
            slots(ui.menuQuestSlot)
            elements {
                templates
            }
            set(ui.menuQuestSlotFilter, ui.items[ItemType.FILTER]!!.getItemStack(profile, ui, unavailable)) {
                UIMenuFilter(ui, profile).open()
            }
            onGenerate(async = true) { _, template, _, _ ->
                ui.items[template.itemType]!!.getItemStack(profile, ui, template.template)
            }
            onBuild(async = true) { inventory ->
                inventory.setItem(ui.menuQuestSlotInfo, ui.items[ItemType.INFO]!!.getItemStack(profile, ui, unavailable))
                val item = ui.items[ItemType.QUEST_UNAVAILABLE]!!.getItemStack(profile, ui, unavailable)
                ui.menuQuestSlot.forEach {
                    if (inventory.getItem(it).isAir()) {
                        inventory.setItem(it, item)
                    }
                }
            }
            onClick { event ->
                if (event.rawSlot == -999) {
                    when {
                        // 左键翻页
                        event.clickEvent().isLeftClick -> {
                            if (hasPreviousPage()) {
                                open(page - 1)
                            }
                        }
                        // 右键翻页
                        event.clickEvent().isRightClick -> {
                            if (hasNextPage()) {
                                open(page + 1)
                            }
                        }
                    }
                }
            }
            onClick { event, el ->
                // 当任务为正在进行或可以开始时
                if (el.itemType == ItemType.QUEST_STARTED || el.itemType == ItemType.QUEST_STARTED_SHARED || el.itemType == ItemType.QUEST_CAN_START) {
                    // 关闭追踪
                    if (el.template == profile.trackQuest) {
                        // 放弃追踪
                        profile.trackQuest = null
                        // 播放音效并关闭界面
                        event.clicker.playSound(event.clicker.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f)
                    }
                    // 当任务允许被追踪时才会关闭界面
                    else if (el.template.allowTracked()) {
                        // 追踪任务
                        profile.trackQuest = el.template
                        // 播放音效并关闭界面
                        event.clicker.playSound(event.clicker.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f)
                    }
                }
            }
        }
    }

    companion object {

        private val unavailable = Template("__unavailable__", Configuration.empty())
    }
}