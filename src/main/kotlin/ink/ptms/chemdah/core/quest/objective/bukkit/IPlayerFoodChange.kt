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
    override val event = FoodLevelChangeEvent::class.java

    init {
        handler {
            it.entity as Player
        }
        addSimpleCondition("position") { data, e ->
            data.toPosition().inside(e.entity.location)
        }
        addSimpleCondition("amount") { data, e ->
            data.toInt() <= e.foodLevel
        }
        addSimpleCondition("item") { data, e ->
            data.toInferItem().isItem(e.item ?: EMPTY_ITEM)
        }
        addConditionVariable("amount") {
            it.foodLevel
        }
    }

    override fun getCount(profile: PlayerProfile, task: Task, event: FoodLevelChangeEvent): Int {
        return event.foodLevel
    }
}