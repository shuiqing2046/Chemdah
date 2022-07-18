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
    override val isAsync = true

    init {
        handler {
            if (it.clickedBlock != null) it.player else null
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
            data.toInferItem().isItem(e.item ?: EMPTY_ITEM)
        }
    }
}