package ink.ptms.chemdah.core.quest.objective.sandalphon

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountable
import ink.ptms.sandalphon.module.impl.blockmine.event.BlockBreakEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.sandalphon.SBlockBreak
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("Sandalphon")
object SBlockBreak : ObjectiveCountable<BlockBreakEvent>() {

    override val name = "sandalphon block break"
    override val event = BlockBreakEvent::class

    init {
        handler {
            player
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("position") || task.condition["position"]!!.toPosition().inside(e.bukkitEvent.block.location)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("material") || task.condition["material"]!!.toInferBlock().isBlock(e.bukkitEvent.block)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("id") || task.condition["id"]!!.toString().equals(e.blockData.id, true)
        }
    }
}