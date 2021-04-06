package ink.ptms.chemdah.module.ui

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Template
import io.izzel.taboolib.kotlin.Tasks
import io.izzel.taboolib.module.db.local.SecuredFile
import io.izzel.taboolib.util.item.Items
import io.izzel.taboolib.util.item.inventory.ClickEvent
import io.izzel.taboolib.util.item.inventory.linked.MenuLinked
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

/**
 * Chemdah
 * ink.ptms.chemdah.module.ui.UIMenu
 *
 * @author sky
 * @since 2021/3/25 7:18 下午
 */
class UIMenuFilter(val ui: UI, val profile: PlayerProfile) : MenuLinked<Include>(profile.player) {

    init {
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
        return ui.menuFilterRows
    }

    override fun getElements(): List<Include> {
        return ui.include
    }

    override fun getSlots(): List<Int> {
        return ui.menuFilterSlot
    }

    override fun onBuild(inventory: Inventory) {
    }

    override fun onBuildAsync(inventory: Inventory) {
        val item = ui.items[ItemType.QUEST_UNAVAILABLE]!!.getItemStack(profile, ui, unavailable)
        ui.menuFilterSlot.forEach {
            if (Items.isNull(inventory.getItem(it))) {
                inventory.setItem(it, item)
            }
        }
    }

    override fun onClick(event: ClickEvent, include: Include) {
        val includes = ui.playerFilters.computeIfAbsent(player.uniqueId) { ArrayList() }
        if (includes.contains(include.id)) {
            includes.remove(include.id)
            event.currentItem = include.normalItem
        } else {
            includes.add(include.id)
            event.currentItem = include.activeItem
        }
        event.clicker.playSound(event.clicker.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 2f)
    }

    override fun generateItem(player: Player, include: Include, index: Int, slot: Int): ItemStack {
        val includes = ui.playerFilters.computeIfAbsent(player.uniqueId) { ArrayList() }
        return if (include.id in includes) include.activeItem else include.normalItem
    }

    override fun onClose(e: InventoryCloseEvent) {
        Tasks.delay(1) {
            ui.open(profile)
        }
    }

    companion object {

        private val unavailable = Template("__unavailable__", SecuredFile())
    }
}