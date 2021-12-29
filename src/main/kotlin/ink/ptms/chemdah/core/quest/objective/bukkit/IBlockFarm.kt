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
    override val event = PlayerInteractEvent::class.java

    init {
        handler {
            if (it.action == Action.RIGHT_CLICK_BLOCK && it.clickedBlock!!.type.isFarmable() && it.item?.type?.name?.endsWith("_HOE") == true) {
                it.player
            } else {
                null
            }
        }
        addSimpleCondition("position") { data, e ->
            data.toPosition().inside(e.clickedBlock!!.location)
        }
        addSimpleCondition("material") { data, e ->
            data.toInferBlock().isBlock(e.clickedBlock!!)
        }
        addSimpleCondition("action") { data, e ->
            data.asList().any { it.equals(e.action.name, true) }
        }
        addSimpleCondition("face") { data, e ->
            data.asList().any { it.equals(e.blockFace.name, true) }
        }
        addSimpleCondition("hand") { data, e ->
            data.asList().any { it.equals(e.hand?.name, true) }
        }
        addSimpleCondition("item") { data, e ->
            data.toInferItem().isItem(e.item!!)
        }
    }

    private fun Material.isFarmable(): Boolean {
        return name == "DIRT" || name == "GRASS_BLOCK" || name == "GRASS_PATH"
    }
}