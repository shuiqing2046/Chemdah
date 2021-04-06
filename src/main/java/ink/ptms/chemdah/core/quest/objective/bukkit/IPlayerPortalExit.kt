package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountable
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityPortalExitEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerPortalExit
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerPortalExit : ObjectiveCountable<EntityPortalExitEvent>() {

    override val name = "portal exit"
    override val event = EntityPortalExitEvent::class

    init {
        handler {
            if (entity is Player) entity as Player else null
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("position") || task.condition["position"]!!.toPosition().inside(e.to ?: EMPTY)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("position:from") || task.condition["position:from"]!!.toPosition().inside(e.from)
        }
    }
}