package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountable
import org.bukkit.event.block.BlockIgniteEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IBlockIgnite
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IBlockIgnite : ObjectiveCountable<BlockIgniteEvent>() {

    override val name = "block ignite"
    override val event = BlockIgniteEvent::class

    init {
        handler {
            player
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("position") || task.condition["position"]!!.toPosition().inside(e.block.location)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("material") || task.condition["material"]!!.toInferBlock().isBlock(e.block)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("material:igniting") || task.condition["material:igniting"]!!.toInferBlock().isBlock(e.ignitingBlock ?: return@addCondition false)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("cause") || task.condition["cause"]!!.asList().any { it.equals(e.cause.name, true) }
        }
    }
}