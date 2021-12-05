package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityPortalEnterEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerPortalEnter
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerPortalEnter : ObjectiveCountableI<EntityPortalEnterEvent>() {

    override val name = "portal enter"
    override val event = EntityPortalEnterEvent::class

    init {
        handler {
            entity as? Player
        }
        addSimpleCondition("position") { e ->
            toPosition().inside(e.location)
        }
    }
}