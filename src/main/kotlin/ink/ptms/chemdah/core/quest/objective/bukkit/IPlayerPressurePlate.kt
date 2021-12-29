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
            if (it.action == Action.PHYSICAL && it.clickedBlock?.type?.name?.endsWith("PRESSURE_PLATE") == true) it.player else null
        }
        addSimpleCondition("position") { data, e ->
            data.toPosition().inside(e.clickedBlock!!.location)
        }
        addSimpleCondition("material") { data, e ->
            data.toInferBlock().isBlock(e.clickedBlock!!)
        }
    }
}