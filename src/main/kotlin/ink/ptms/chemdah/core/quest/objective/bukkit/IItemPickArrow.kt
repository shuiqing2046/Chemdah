package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Task
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.event.player.PlayerPickupArrowEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IItemPickArrow
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IItemPickArrow : ObjectiveCountableI<PlayerPickupArrowEvent>() {

    override val name = "pickup arrow"
    override val event = PlayerPickupArrowEvent::class

    init {
        handler {
            player
        }
        addCondition("position") { e ->
            toPosition().inside(e.player.location)
        }
        addCondition("item") { e ->
            toInferItem().isItem(e.item.itemStack)
        }
        addCondition("amount") { e ->
            toInt() <= e.item.itemStack.amount
        }
        addConditionVariable("amount") {
            it.item.itemStack.amount
        }
    }

    override fun getCount(profile: PlayerProfile, task: Task, event: PlayerPickupArrowEvent): Int {
        return event.item.itemStack.amount
    }
}