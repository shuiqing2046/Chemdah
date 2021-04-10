package ink.ptms.chemdah.core.quest.objective.bukkit

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Task
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountable
import org.bukkit.event.player.PlayerPickupArrowEvent
import org.bukkit.event.player.PlayerRecipeDiscoverEvent
import org.bukkit.event.player.PlayerStatisticIncrementEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerStatistic
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerStatistic : ObjectiveCountable<PlayerStatisticIncrementEvent>() {

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
}