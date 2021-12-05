package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.Material
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IBlockInteract
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IBlockFarm : ObjectiveCountableI<PlayerInteractEvent>() {

    override val name = "block farm"
    override val event = PlayerInteractEvent::class

    init {
        handler {
            if (action == Action.RIGHT_CLICK_BLOCK && clickedBlock!!.type.isFarmable() && item?.type?.name?.endsWith("_HOE") == true) player else null
        }
        addSimpleCondition("position") { e ->
            toPosition().inside(e.clickedBlock!!.location)
        }
        addSimpleCondition("material") { e ->
            toInferBlock().isBlock(e.clickedBlock!!)
        }
        addSimpleCondition("action") { e ->
            asList().any { it.equals(e.action.name, true) }
        }
        addSimpleCondition("face") { e ->
            asList().any { it.equals(e.blockFace.name, true) }
        }
        addSimpleCondition("hand") { e ->
            asList().any { it.equals(e.hand?.name, true) }
        }
        addSimpleCondition("item") { e ->
            toInferItem().isItem(e.item!!)
        }
    }

    private fun Material.isFarmable(): Boolean {
        return name == "DIRT" || name == "GRASS_BLOCK" || name == "GRASS_PATH"
    }
}