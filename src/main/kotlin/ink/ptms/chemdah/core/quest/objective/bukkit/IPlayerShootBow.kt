package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityShootBowEvent

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
    override val event = EntityShootBowEvent::class

    init {
        handler {
            entity as? Player
        }
        addSimpleCondition("position") { e ->
            toPosition().inside(e.entity.location)
        }
        addSimpleCondition("arrow") { e ->
            toInferEntity().isEntity(e.projectile)
        }
        addSimpleCondition("item") { e ->
            toInferItem().isItem(e.bow ?: AIR)
        }
        addSimpleCondition("item:consumable") { e ->
            toInferItem().isItem(e.consumable ?: AIR)
        }
        addSimpleCondition("hand") { e ->
            asList().any { it.equals(e.hand.name, true) }
        }
        addSimpleCondition("force") { e ->
            toDouble() <= e.force
        }
        addSimpleCondition("consumable") { e ->
            toBoolean() == e.shouldConsumeItem()
        }
        addConditionVariable("force") {
            it.force
        }
    }
}