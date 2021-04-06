package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountable
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
object IPlayerShootBow : ObjectiveCountable<EntityShootBowEvent>() {

    override val name = "shoot bow"
    override val event = EntityShootBowEvent::class

    init {
        handler {
            if (entity is Player) entity as Player else null
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("position") || task.condition["position"]!!.toPosition().inside(e.entity.location)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("arrow") || task.condition["arrow"]!!.toInferEntity().isEntity(e.projectile)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("item") || task.condition["item"]!!.toInferItem().isItem(e.bow ?: AIR)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("item:consumable") || task.condition["item:consumable"]!!.toInferItem().isItem(e.consumable ?: AIR)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("hand") || task.condition["hand"]!!.asList().any { it.equals(e.hand.name, true) }
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("force") || task.condition["force"]!!.toDouble() <= e.force
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("consumable") || task.condition["consumable"]!!.toBoolean() == e.shouldConsumeItem()
        }
    }
}