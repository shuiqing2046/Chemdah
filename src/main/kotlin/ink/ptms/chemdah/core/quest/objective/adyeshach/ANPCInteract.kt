package ink.ptms.chemdah.core.quest.objective.adyeshach

import ink.ptms.adyeshach.api.event.AdyeshachEntityInteractEvent
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.util.Vector

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.adyeshach.ANPCInteract
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("Adyeshach")
object ANPCInteract : ObjectiveCountableI<AdyeshachEntityInteractEvent>() {

    override val name = "anpc interact"
    override val event = AdyeshachEntityInteractEvent::class.java

    init {
        handler {
            player
        }
        addSimpleCondition("position") { e ->
            toPosition().inside(e.entity.getLocation())
        }
        addSimpleCondition("position:clicked") { e ->
            toVector().inside(Vector(e.vector.x, e.vector.y, e.vector.z))
        }
        addSimpleCondition("id") { e ->
            asList().any { it.equals(e.entity.id, true) }
        }
        addSimpleCondition("type") { e ->
            asList().any { it.equals(e.entity.entityType.name, true) }
        }
        addSimpleCondition("hand") { e ->
            toBoolean() == e.isMainHand
        }
        addSimpleCondition("item") { e ->
            toInferItem().isItem(if (e.isMainHand) e.player.inventory.itemInMainHand else e.player.inventory.itemInOffHand)
        }
        addConditionVariable("id") {
            it.entity.id
        }
    }
}