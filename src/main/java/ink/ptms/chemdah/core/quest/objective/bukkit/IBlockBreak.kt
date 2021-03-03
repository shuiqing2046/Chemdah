package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.ObjectiveCountable
import org.bukkit.event.block.BlockBreakEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.impl.IBlockBreak
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
object IBlockBreak : ObjectiveCountable<BlockBreakEvent>() {

    override val name = "block break"
    override val event = BlockBreakEvent::class

    init {
        handler {
            player
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("position") || task.condition["position"]!!.toPosition().inside(e.block.location)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("material") || task.condition["position"]!!.toMaterial().isBlock(e.block)
        }
    }
}