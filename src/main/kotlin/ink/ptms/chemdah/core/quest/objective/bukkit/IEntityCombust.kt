package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityCombustByEntityEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IEntityCombust
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IEntityCombust : ObjectiveCountableI<EntityCombustByEntityEvent>() {

    override val name = "entity combust"
    override val event = EntityCombustByEntityEvent::class

    init {
        handler {
            combuster as? Player
        }
        addSimpleCondition("position") { e ->
            toPosition().inside(e.entity.location)
        }
        addSimpleCondition("entity") { e ->
            toInferEntity().isEntity(e.entity)
        }
    }
}