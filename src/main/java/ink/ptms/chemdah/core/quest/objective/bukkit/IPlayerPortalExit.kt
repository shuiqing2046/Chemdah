package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
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
object IPlayerPortalExit : ObjectiveCountableI<EntityPortalExitEvent>() {

    override val name = "portal exit"
    override val event = EntityPortalExitEvent::class

    init {
        handler {
            entity as? Player
        }
        addCondition("position") { e ->
            toPosition().inside(e.to ?: EMPTY)
        }
        addCondition("position:to") { e ->
            toPosition().inside(e.to ?: EMPTY)
        }
        addCondition("position:from") { e ->
            toPosition().inside(e.from)
        }
    }
}