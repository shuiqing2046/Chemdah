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
    override val event = PlayerInteractEvent::class.java

    init {
        handler {
            if (clickedBlock != null) player else null
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
}