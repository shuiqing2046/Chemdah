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
    override val event = BlockPlaceEvent::class.java
    override val isAsync = true

    init {
        handler {
            it.player
        }
        addSimpleCondition("position") { data, e ->
            data.toPosition().inside(e.block.location)
        }
        addSimpleCondition("material") { data, e ->
            data.toInferBlock().isBlock(e.block)
        }
        addSimpleCondition("material:against") { data, e ->
            data.toInferBlock().isBlock(e.blockAgainst)
        }
        addSimpleCondition("hand") { data, e ->
            data.asList().any { it.equals(e.hand.name, true) }
        }
    }
}