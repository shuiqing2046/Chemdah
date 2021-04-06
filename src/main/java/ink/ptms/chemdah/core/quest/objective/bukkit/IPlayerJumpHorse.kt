package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountable
import org.bukkit.entity.Player
import org.bukkit.event.entity.HorseJumpEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerJump
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerJumpHorse : ObjectiveCountable<HorseJumpEvent>() {

    override val name = "horse jump"
    override val event = HorseJumpEvent::class

    init {
        handler {
            if (entity.passengers[0] is Player) entity.passengers[0] as Player else null
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("position") || task.condition["position"]!!.toPosition().inside(e.entity.location)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("entity") || task.condition["entity"]!!.toInferEntity().isEntity(e.entity)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("power") || task.condition["power"]!!.toDouble() <= e.power
        }
    }
}