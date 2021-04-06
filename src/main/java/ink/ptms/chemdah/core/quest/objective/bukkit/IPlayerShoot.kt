package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountable
import org.bukkit.entity.Player
import org.bukkit.event.entity.ProjectileLaunchEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerShoot
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerShoot : ObjectiveCountable<ProjectileLaunchEvent>() {

    override val name = "shoot projectile"
    override val event = ProjectileLaunchEvent::class

    init {
        handler {
            if (entity.shooter is Player) entity.shooter as Player else null
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("position") || task.condition["position"]!!.toPosition().inside(e.entity.location)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("projectile") || task.condition["projectile"]!!.toInferEntity().isEntity(e.entity)
        }
    }
}