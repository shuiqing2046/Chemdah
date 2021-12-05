package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Task
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.entity.Player
import org.bukkit.event.entity.FoodLevelChangeEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerFoodChange
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerFoodChange : ObjectiveCountableI<FoodLevelChangeEvent>() {

    override val name = "food change"
    override val event = FoodLevelChangeEvent::class

    init {
        handler {
            entity as Player
        }
        addSimpleCondition("position") { e ->
            toPosition().inside(e.entity.location)
        }
        addSimpleCondition("amount") { e ->
            toInt() <= e.foodLevel
        }
        addSimpleCondition("item") { e ->
            toInferItem().isItem(e.item ?: AIR)
        }
        addConditionVariable("amount") {
            it.foodLevel
        }
    }

    override fun getCount(profile: PlayerProfile, task: Task, event: FoodLevelChangeEvent): Int {
        return event.foodLevel
    }
}