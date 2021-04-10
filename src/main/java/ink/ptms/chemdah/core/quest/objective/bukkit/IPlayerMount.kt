package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountable
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerShearEntityEvent
import org.spigotmc.event.entity.EntityMountEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerMount
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerMount : ObjectiveCountable<EntityMountEvent>() {

    override val name = "entity mount"
    override val event = EntityMountEvent::class

    init {
        handler {
            entity as? Player
        }
        addCondition("position") { e ->
            toPosition().inside(e.mount.location)
        }
        addCondition("entity") { e ->
            toInferEntity().isEntity(e.mount)
        }
    }
}