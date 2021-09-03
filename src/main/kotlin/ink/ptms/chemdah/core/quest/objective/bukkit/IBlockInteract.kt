package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.event.player.PlayerInteractEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IBlockInteract
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IBlockInteract : ObjectiveCountableI<PlayerInteractEvent>() {

    override val name = "block interact"
    override val event = PlayerInteractEvent::class

    init {
        handler {
            if (clickedBlock != null) player else null
        }
        addCondition("position") { e ->
            toPosition().inside(e.clickedBlock!!.location)
        }
        addCondition("material") { e ->
            toInferBlock().isBlock(e.clickedBlock!!)
        }
        addCondition("action") { e ->
            asList().any { it.equals(e.action.name, true) }
        }
        addCondition("face") { e ->
            asList().any { it.equals(e.blockFace.name, true) }
        }
        addCondition("hand") { e ->
            asList().any { it.equals(e.hand?.name, true) }
        }
        addCondition("item") { e ->
            toInferItem().isItem(e.item!!)
        }
    }
}