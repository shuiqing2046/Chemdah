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
    override val event = PlayerInteractAtEntityEvent::class.java

    init {
        handler {
            it.player
        }
        addSimpleCondition("position") { data, e ->
            data.toPosition().inside(e.rightClicked.location)
        }
        addSimpleCondition("position:clicked") { data, e ->
            data.toVector().inside(e.clickedPosition)
        }
        addSimpleCondition("entity") { data, e ->
            data.toInferEntity().isEntity(e.rightClicked)
        }
        addSimpleCondition("hand") { data, e ->
            data.asList().any { it.equals(e.hand.name, true) }
        }
        addSimpleCondition("item") { data, e ->
            data.toInferItem().isItem(e.player.equipment!!.getItem(e.hand))
        }
    }
}