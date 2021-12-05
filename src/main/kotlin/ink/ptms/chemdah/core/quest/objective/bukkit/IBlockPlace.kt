package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.event.block.BlockPlaceEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IBlockPlace
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IBlockPlace : ObjectiveCountableI<BlockPlaceEvent>() {

    override val name = "block place"
    override val event = BlockPlaceEvent::class

    init {
        handler {
            player
        }
        addSimpleCondition("position") { e ->
            toPosition().inside(e.block.location)
        }
        addSimpleCondition("material") { e ->
            toInferBlock().isBlock(e.block)
        }
        addSimpleCondition("material:against") { e ->
            toInferBlock().isBlock(e.blockAgainst)
        }
        addSimpleCondition("hand") { e ->
            asList().any { it.equals(e.hand.name, true) }
        }
    }
}