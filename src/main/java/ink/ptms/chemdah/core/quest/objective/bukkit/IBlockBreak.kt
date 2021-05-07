package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.event.block.BlockBreakEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IBlockBreak
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IBlockBreak : ObjectiveCountableI<BlockBreakEvent>() {

    override val name = "block break"
    override val event = BlockBreakEvent::class

    init {
        handler {
            player
        }
        addCondition("position") { 
            toPosition().inside(it.block.location)
        }
        addCondition("material") {
            toInferBlock().isBlock(it.block)
        }
        addCondition("exp") { 
            toInt() <= it.expToDrop
        }
        addConditionVariable("exp") {
            it.expToDrop
        }
    }
}