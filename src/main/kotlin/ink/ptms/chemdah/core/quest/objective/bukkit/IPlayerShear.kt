package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.event.player.PlayerShearEntityEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerShear
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerShear : ObjectiveCountableI<PlayerShearEntityEvent>() {

    override val name = "player shear"
    override val event = PlayerShearEntityEvent::class

    init {
        handler {
            player
        }
        addSimpleCondition("position") { e ->
            toPosition().inside(e.entity.location)
        }
        addSimpleCondition("entity") { e ->
            toInferEntity().isEntity(e.entity)
        }
        addSimpleCondition("item") { e ->
            toInferItem().isItem(e.item)
        }
        addSimpleCondition("hand") { e ->
            asList().any { it.equals(e.hand.name, true) }
        }
    }
}