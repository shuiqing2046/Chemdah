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
    override val event = EntityPortalExitEvent::class.java

    init {
        handler {
            it.entity as? Player
        }
        addSimpleCondition("position") { data, e ->
            data.toPosition().inside(e.to ?: EMPTY_LOCATION)
        }
        addSimpleCondition("position:to") { data, e ->
            data.toPosition().inside(e.to ?: EMPTY_LOCATION)
        }
        addSimpleCondition("position:from") { data, e ->
            data.toPosition().inside(e.from)
        }
    }
}