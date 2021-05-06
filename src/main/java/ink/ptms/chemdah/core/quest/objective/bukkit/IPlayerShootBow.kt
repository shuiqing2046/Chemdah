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
        addCondition("position") { e ->
            toPosition().inside(e.entity.location)
        }
        addCondition("arrow") { e ->
            toInferEntity().isEntity(e.projectile)
        }
        addCondition("item") { e ->
            toInferItem().isItem(e.bow ?: AIR)
        }
        addCondition("item:consumable") { e ->
            toInferItem().isItem(e.consumable ?: AIR)
        }
        addCondition("hand") { e ->
            asList().any { it.equals(e.hand.name, true) }
        }
        addCondition("force") { e ->
            toDouble() <= e.force
        }
        addCondition("consumable") { e ->
            toBoolean() == e.shouldConsumeItem()
        }
        addConditionVariable("force") {
            it.force
        }
    }
}