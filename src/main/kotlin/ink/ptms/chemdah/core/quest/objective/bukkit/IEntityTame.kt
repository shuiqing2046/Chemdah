package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityTameEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IEntityTame
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IEntityTame : ObjectiveCountableI<EntityTameEvent>() {

    override val name = "entity tame"
    override val event = EntityTameEvent::class.java

    init {
        handler {
            owner as? Player
        }
        addSimpleCondition("position") { e ->
            toPosition().inside(e.entity.location)
        }
        addSimpleCondition("entity") { e ->
            toInferEntity().isEntity(e.entity)
        }
    }
}