package ink.ptms.chemdah.module.ui

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Template
import ink.ptms.chemdah.util.replace
import org.bukkit.Sound
import taboolib.common.platform.function.submit
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
class UIMenuFilter(val ui: UI, val profile: PlayerProfile) {

    fun open(page: Int = 0) {
        profile.player.openMenu<Linked<Include>>(ui.name.replace("name" to profile.player.name, "page" to "%p")) {
            page(page)
            rows(ui.menuFilterRows)
            slots(ui.menuFilterSlot)
            elements {
                ui.include
            }
            onBuild(async = true) { inventory ->
                val item = ui.items[ItemType.QUEST_UNAVAILABLE]!!.getItemStack(profile, ui, unavailable)
                ui.menuFilterSlot.forEach {
                    if (inventory.getItem(it).isAir()) {
                        inventory.setItem(it, item)
                    }
                }
            }
            onClick { event ->
                if (event.rawSlot == -999) {
                    when {
                        event.clickEvent().isLeftClick -> {
                            if (hasPreviousPage()) {
                                open(page - 1)
                            }
                        }
                        event.clickEvent().isRightClick -> {
                            if (hasNextPage()) {
                                open(page + 1)
                            }
                        }
                    }
                }
            }
            onClick { event, include ->
                val includes = ui.playerFilters.computeIfAbsent(event.clicker.uniqueId) { ArrayList() }
                if (includes.contains(include.id)) {
                    includes.remove(include.id)
                    event.currentItem = include.normalItem
                } else {
                    includes.add(include.id)
                    event.currentItem = include.activeItem
                }
                event.clicker.playSound(event.clicker.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 2f)
            }
            onGenerate(async = true) { player, include, _, _ ->
                val includes = ui.playerFilters.computeIfAbsent(player.uniqueId) { ArrayList() }
                if (include.id in includes) include.activeItem else include.normalItem
            }
            onClose {
                submit(delay = 1) {
                    ui.open(profile)
                }
            }
        }
    }

    companion object {

        private val unavailable = Template("__unavailable__", Configuration.empty())
    }
}