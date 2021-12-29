package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
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
object IPlayerJumpHorse : ObjectiveCountableI<HorseJumpEvent>() {

    override val name = "horse jump"
    override val event = HorseJumpEvent::class.java
    override val isAsync = true

    init {
        handler {
            it.entity.passengers[0] as? Player
        }
        addSimpleCondition("position") { data, e ->
            data.toPosition().inside(e.entity.location)
        }
        addSimpleCondition("entity") { data, e ->
            data.toInferEntity().isEntity(e.entity)
        }
        addSimpleCondition("power") { data, e ->
            data.toDouble() <= e.power
        }
        addConditionVariable("power") {
            it.power
        }
    }
}