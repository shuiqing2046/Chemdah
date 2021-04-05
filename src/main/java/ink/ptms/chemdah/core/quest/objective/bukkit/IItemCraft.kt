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

    override val name = "item craft"
    override val event = CraftItemEvent::class

    init {
        handler {
            if (Items.nonNull(inventory.result)) whoClicked as Player else null
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("position") || task.condition["position"]!!.toPosition().inside(e.whoClicked.location)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("item") || task.condition["item"]!!.toItem().match(e.inventory.result!!)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("item:matrix") || task.condition["item:matrix"]!!.toItem().run { e.inventory.matrix.any { item -> match(item) } }
        }
    }
}