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
    override val event = PlayerShearEntityEvent::class.java

    init {
        handler {
            it.player
        }
        addSimpleCondition("position") { data, e ->
            data.toPosition().inside(e.entity.location)
        }
        addSimpleCondition("entity") { data, e ->
            data.toInferEntity().isEntity(e.entity)
        }
        addSimpleCondition("item") { data, e ->
            data.toInferItem().isItem(e.item)
        }
        addSimpleCondition("hand") { data, e ->
            data.asList().any { it.equals(e.hand.name, true) }
        }
    }
}