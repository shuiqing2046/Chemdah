package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountable
import org.bukkit.event.player.PlayerFishEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerFish
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerFish : ObjectiveCountable<PlayerFishEvent>() {

    override val name = "player fish"
    override val event = PlayerFishEvent::class

    init {
        handler {
            player
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("position") || task.condition["position"]!!.toPosition().inside(e.player.location)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("entity") || task.condition["entity"]!!.toInferEntity().isEntity(e.caught)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("entity:hook") || task.condition["entity:hook"]!!.toInferEntity().isEntity(e.hook)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("state") || task.condition["state"]!!.asList().any { it.equals(e.state.name, true) }
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("exp") || task.condition["exp"]!!.toInt() <= e.expToDrop
        }
    }
}