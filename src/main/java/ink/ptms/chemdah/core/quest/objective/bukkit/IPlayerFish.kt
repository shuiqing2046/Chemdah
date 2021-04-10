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
        addCondition("position") { e ->
            toPosition().inside(e.player.location)
        }
        addCondition("entity") { e ->
            toInferEntity().isEntity(e.caught)
        }
        addCondition("entity:hook") { e ->
            toInferEntity().isEntity(e.hook)
        }
        addCondition("state") { e ->
            asList().any { it.equals(e.state.name, true) }
        }
        addCondition("exp") { e ->
            toInt() <= e.expToDrop
        }
        addConditionVariable("exp") {
            it.expToDrop
        }
    }
}