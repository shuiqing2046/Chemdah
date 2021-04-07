package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountable
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
object IBlockFarm : ObjectiveCountable<PlayerInteractEvent>() {

    override val name = "block farm"
    override val event = PlayerInteractEvent::class

    init {
        handler {
            if (action == Action.RIGHT_CLICK_BLOCK && clickedBlock!!.type.isFarmable() && item?.type?.name?.endsWith("_HOE") == true) player else null
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("position") || task.condition["position"]!!.toPosition().inside(e.clickedBlock!!.location)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("material") || task.condition["material"]!!.toInferBlock().isBlock(e.clickedBlock!!)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("action") || task.condition["action"]!!.asList().any { it.equals(e.action.name, true) }
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("face") || task.condition["face"]!!.asList().any { it.equals(e.blockFace.name, true) }
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("hand") || task.condition["hand"]!!.asList().any { it.equals(e.hand?.name, true) }
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("item") || task.condition["item"]!!.toInferItem().isItem(e.item!!)
        }
    }

    private fun Material.isFarmable(): Boolean {
        return name == "DIRT" || name == "GRASS_BLOCK" || name == "GRASS_PATH"
    }
}