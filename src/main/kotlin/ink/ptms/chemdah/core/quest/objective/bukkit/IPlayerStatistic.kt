package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Task
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.event.player.PlayerStatisticIncrementEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerStatistic
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerStatistic : ObjectiveCountableI<PlayerStatisticIncrementEvent>() {

    override val name = "player statistic"
    override val event = PlayerStatisticIncrementEvent::class

    init {
        handler {
            player
        }
        addCondition("position") { e ->
            toPosition().inside(e.player.location)
        }
        addCondition("statistic") { e ->
            asList().any { it.equals(e.statistic.name, true) }
        }
        addCondition("type:entity") { e ->
            asList().any { it.equals(e.entityType?.name, true) }
        }
        addCondition("type:material") { e ->
            asList().any { it.equals(e.material?.name, true) }
        }
        addCondition("value") { e ->
            toInt() <= e.newValue
        }
        addCondition("value:new") { e ->
            toInt() <= e.newValue
        }
        addCondition("value:previous") { e ->
            toInt() <= e.previousValue
        }
        addConditionVariable("value") {
            it.newValue
        }
        addConditionVariable("value:new") {
            it.newValue
        }
        addConditionVariable("value:previous") {
            it.previousValue
        }
    }

    override fun getCount(profile: PlayerProfile, task: Task, event: PlayerStatisticIncrementEvent): Int {
        return event.newValue - event.previousValue
    }
}