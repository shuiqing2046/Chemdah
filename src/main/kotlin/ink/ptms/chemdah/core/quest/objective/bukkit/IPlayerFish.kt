package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.event.player.PlayerFishEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerFish
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerFish : ObjectiveCountableI<PlayerFishEvent>() {

    override val name = "player fish"
    override val event = PlayerFishEvent::class

    init {
        handler {
            player
        }
        addSimpleCondition("position") { e ->
            toPosition().inside(e.player.location)
        }
        addSimpleCondition("entity") { e ->
            toInferEntity().isEntity(e.caught)
        }
        addSimpleCondition("entity:hook") { e ->
            toInferEntity().isEntity(e.hook)
        }
        addSimpleCondition("state") { e ->
            asList().any { it.equals(e.state.name, true) }
        }
        addSimpleCondition("exp") { e ->
            toInt() <= e.expToDrop
        }
        addConditionVariable("exp") {
            it.expToDrop
        }
    }
}