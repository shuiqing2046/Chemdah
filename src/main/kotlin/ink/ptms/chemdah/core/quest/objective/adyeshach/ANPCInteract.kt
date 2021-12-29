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
            it.player
        }
        addSimpleCondition("position") { data, e ->
            data.toPosition().inside(e.entity.getLocation())
        }
        addSimpleCondition("position:clicked") { data, e ->
            data.toVector().inside(Vector(e.vector.x, e.vector.y, e.vector.z))
        }
        addSimpleCondition("id") { data, e ->
            data.asList().any { it.equals(e.entity.id, true) }
        }
        addSimpleCondition("type") { data, e ->
            data.asList().any { it.equals(e.entity.entityType.name, true) }
        }
        addSimpleCondition("hand") { data, e ->
            data.toBoolean() == e.isMainHand
        }
        addSimpleCondition("item") { data, e ->
            data.toInferItem().isItem(if (e.isMainHand) e.player.inventory.itemInMainHand else e.player.inventory.itemInOffHand)
        }
        addConditionVariable("id") {
            it.entity.id
        }
    }
}