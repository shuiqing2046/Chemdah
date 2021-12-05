package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.event.player.PlayerInteractAtEntityEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IEntityInteract
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IEntityInteract : ObjectiveCountableI<PlayerInteractAtEntityEvent>() {

    override val name = "entity interact"
    override val event = PlayerInteractAtEntityEvent::class

    init {
        handler {
            player
        }
        addSimpleCondition("position") { e ->
            toPosition().inside(e.rightClicked.location)
        }
        addSimpleCondition("position:clicked") { e ->
            toVector().inside(e.clickedPosition)
        }
        addSimpleCondition("entity") { e ->
            toInferEntity().isEntity(e.rightClicked)
        }
        addSimpleCondition("hand") { e ->
            asList().any { it.equals(e.hand.name, true) }
        }
        addSimpleCondition("item") { e ->
            toInferItem().isItem(e.player.equipment!!.getItem(e.hand))
        }
    }
}