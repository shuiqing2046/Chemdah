package ink.ptms.chemdah.core.quest.objective.adyeshach

import ink.ptms.adyeshach.api.event.AdyeshachEntityDamageEvent
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.adyeshach.ANPCDamage
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("Adyeshach")
object ANPCDamage : ObjectiveCountableI<AdyeshachEntityDamageEvent>() {

    override val name = "anpc damage"
    override val event = AdyeshachEntityDamageEvent::class.java

    init {
        handler {
            it.player
        }
        addSimpleCondition("position") { data, e ->
            data.toPosition().inside(e.entity.getLocation())
        }
        addSimpleCondition("id") { data, e ->
            data.asList().any { it.equals(e.entity.id, true) }
        }
        addSimpleCondition("type") { data, e ->
            data.asList().any { it.equals(e.entity.entityType.name, true) }
        }
        addSimpleCondition("item") { data, e ->
            data.toInferItem().isItem(e.player.inventory.itemInMainHand)
        }
        addConditionVariable("id") {
            it.entity.id
        }
    }
}