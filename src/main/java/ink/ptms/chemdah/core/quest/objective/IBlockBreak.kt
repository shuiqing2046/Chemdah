package ink.ptms.chemdah.core.quest.objective

import org.bukkit.event.block.BlockBreakEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.IBlockBreak
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
object IBlockBreak : ObjectiveCountable() {

    override val name = "block break"
    override val event = BlockBreakEvent::class

    init {
        checkCondition<BlockBreakEvent> { _, task, e ->
            !task.condition.containsKey("position") || task.condition["position"]!!.toPosition().inside(e.block.location)
        }
        checkCondition<BlockBreakEvent> { _, task, e ->
            !task.condition.containsKey("material") || task.condition["position"]!!.toMaterial().isBlock(e.block)
        }
    }
}