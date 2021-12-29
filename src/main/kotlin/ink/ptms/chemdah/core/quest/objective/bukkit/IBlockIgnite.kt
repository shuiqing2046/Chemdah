package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.event.block.BlockIgniteEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IBlockIgnite
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IBlockIgnite : ObjectiveCountableI<BlockIgniteEvent>() {

    override val name = "block ignite"
    override val event = BlockIgniteEvent::class.java

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
        addSimpleCondition("material:igniting") { data, e ->
            data.toInferBlock().isBlock(e.ignitingBlock ?: return@addSimpleCondition false)
        }
        addSimpleCondition("cause") { data, e ->
            data.asList().any { it.equals(e.cause.name, true) }
        }
    }
}