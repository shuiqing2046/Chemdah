package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountable
import org.bukkit.event.player.PlayerInteractAtEntityEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IEntityInteract
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IEntityInteract : ObjectiveCountable<PlayerInteractAtEntityEvent>() {

    override val name = "entity interact"
    override val event = PlayerInteractAtEntityEvent::class

    init {
        handler {
            player
        }
        addCondition("position") { e ->
            toPosition().inside(e.rightClicked.location)
        }
        addCondition("position:clicked") { e ->
            toVector().inside(e.clickedPosition)
        }
        addCondition("entity") { e ->
            toInferEntity().isEntity(e.rightClicked)
        }
        addCondition("hand") { e ->
            asList().any { it.equals(e.hand.name, true) }
        }
        addCondition("item") { e ->
            toInferItem().isItem(e.player.equipment!!.getItem(e.hand))
        }
    }
}