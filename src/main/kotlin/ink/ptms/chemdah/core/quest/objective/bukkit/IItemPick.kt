package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Task
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
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
object IItemPick : ObjectiveCountableI<EntityPickupItemEvent>() {

    override val name = "pickup item"
    override val event = EntityPickupItemEvent::class.java

    init {
        handler {
            it.entity as? Player
        }
        addSimpleCondition("position") { data, e ->
            data.toPosition().inside(e.entity.location)
        }
        addSimpleCondition("item") { data, e ->
            data.toInferItem().isItem(e.item.itemStack)
        }
        addSimpleCondition("amount") { data, e ->
            data.toInt() <= e.item.itemStack.amount
        }
        addConditionVariable("amount") {
            it.item.itemStack.amount
        }
    }

    override fun getCount(profile: PlayerProfile, task: Task, event: EntityPickupItemEvent): Int {
        return event.item.itemStack.amount
    }
}