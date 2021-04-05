package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountable
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityPickupItemEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IItemPick
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IItemPick : ObjectiveCountable<EntityPickupItemEvent>() {

    override val name = "item pick"
    override val event = EntityPickupItemEvent::class

    init {
        handler {
            if (entity is Player) entity as Player else null
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("position") || task.condition["position"]!!.toPosition().inside(e.entity.location)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("item") || task.condition["item"]!!.toItem().match(e.item.itemStack)
        }
    }
}