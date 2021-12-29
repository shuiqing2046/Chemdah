package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.entity.Player
import org.spigotmc.event.entity.EntityMountEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerMount
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerMount : ObjectiveCountableI<EntityMountEvent>() {

    override val name = "entity mount"
    override val event = EntityMountEvent::class.java

    init {
        handler {
            it.entity as? Player
        }
        addSimpleCondition("position") { data, e ->
            data.toPosition().inside(e.mount.location)
        }
        addSimpleCondition("entity") { data, e ->
            data.toInferEntity().isEntity(e.mount)
        }
    }
}