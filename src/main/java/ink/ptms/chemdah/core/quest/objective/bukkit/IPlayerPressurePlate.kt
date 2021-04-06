package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountable
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerPressurePlate
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerPressurePlate : ObjectiveCountable<PlayerInteractEvent>() {

    override val name = "pressure plate"
    override val event = PlayerInteractEvent::class

    init {
        handler {
            if (action == Action.PHYSICAL && clickedBlock?.type?.name?.endsWith("PRESSURE_PLATE") == true) player else null
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("position") || task.condition["position"]!!.toPosition().inside(e.clickedBlock!!.location)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("material") || task.condition["material"]!!.toInferBlock().isBlock(e.clickedBlock!!)
        }
    }
}