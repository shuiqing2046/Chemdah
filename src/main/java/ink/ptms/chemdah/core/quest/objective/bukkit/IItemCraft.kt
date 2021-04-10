package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountable
import io.izzel.taboolib.util.item.Items
import org.bukkit.entity.Player
import org.bukkit.event.inventory.CraftItemEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IItemCraft
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IItemCraft : ObjectiveCountable<CraftItemEvent>() {

    override val name = "craft item"
    override val event = CraftItemEvent::class

    init {
        handler {
            if (Items.nonNull(inventory.result)) whoClicked as Player else null
        }
        addCondition("position") { e ->
            toPosition().inside(e.whoClicked.location)
        }
        addCondition("item") { e ->
            toInferItem().isItem(e.inventory.result!!)
        }
        addCondition("item:matrix") { e ->
            toInferItem().run { e.inventory.matrix.any { item -> isItem(item) } }
        }
    }
}