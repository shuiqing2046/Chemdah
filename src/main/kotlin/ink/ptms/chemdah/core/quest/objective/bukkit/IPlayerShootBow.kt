package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityShootBowEvent
import taboolib.common5.cbool

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerShootBow
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerShootBow : ObjectiveCountableI<EntityShootBowEvent>() {

    override val name = "shoot bow"
    override val event = EntityShootBowEvent::class.java

    init {
        handler {
            it.entity as? Player
        }
        addSimpleCondition("position") { data, e ->
            data.toPosition().inside(e.entity.location)
        }
        addSimpleCondition("arrow") { data, e ->
            data.toInferEntity().isEntity(e.projectile)
        }
        addSimpleCondition("item") { data, e ->
            data.toInferItem().isItem(e.bow ?: EMPTY_ITEM)
        }
        addSimpleCondition("item:consumable") { data, e ->
            data.toInferItem().isItem(e.consumable ?: EMPTY_ITEM)
        }
        addSimpleCondition("hand") { data, e ->
            data.asList().any { it.equals(e.hand.name, true) }
        }
        addSimpleCondition("force") { data, e ->
            data.toDouble() <= e.force
        }
        addSimpleCondition("consumable") { data, e ->
            data.cbool == e.shouldConsumeItem()
        }
        addConditionVariable("force") {
            it.force
        }
    }
}