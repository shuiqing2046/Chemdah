package ink.ptms.chemdah.core.quest.objective.adyeshach

import ink.ptms.adyeshach.api.event.AdyeshachEntityInteractEvent
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI

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
    override val event = AdyeshachEntityInteractEvent::class

    init {
        handler {
            player
        }
        addCondition("position") { e ->
            toPosition().inside(e.entity.getLocation())
        }
        addCondition("position:clicked") { e ->
            toVector().inside(e.vector)
        }
        addCondition("id") { e ->
            asList().any { it.equals(e.entity.id, true) }
        }
        addCondition("type") { e ->
            asList().any { it.equals(e.entity.entityType.name, true) }
        }
        addCondition("hand") { e ->
            toBoolean() == e.isMainHand
        }
        addCondition("item") { e ->
            toInferItem().isItem(if (e.isMainHand) e.player.inventory.itemInMainHand else e.player.inventory.itemInOffHand)
        }
        addConditionVariable("id") {
            it.entity.id
        }
    }
}