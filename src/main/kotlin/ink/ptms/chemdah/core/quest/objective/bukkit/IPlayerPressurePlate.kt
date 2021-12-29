package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
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
object IPlayerPressurePlate : ObjectiveCountableI<PlayerInteractEvent>() {

    override val name = "pressure plate"
    override val event = PlayerInteractEvent::class.java

    init {
        handler {
            if (action == Action.PHYSICAL && clickedBlock?.type?.name?.endsWith("PRESSURE_PLATE") == true) player else null
        }
        addSimpleCondition("position") { e ->
            toPosition().inside(e.clickedBlock!!.location)
        }
        addSimpleCondition("material") { e ->
            toInferBlock().isBlock(e.clickedBlock!!)
        }
    }
}