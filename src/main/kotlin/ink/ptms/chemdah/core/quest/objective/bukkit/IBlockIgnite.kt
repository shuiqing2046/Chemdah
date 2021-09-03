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
    override val event = BlockIgniteEvent::class

    init {
        handler {
            player
        }
        addCondition("position") { e ->
            toPosition().inside(e.block.location)
        }
        addCondition("material") { e ->
            toInferBlock().isBlock(e.block)
        }
        addCondition("material:igniting") { e ->
            toInferBlock().isBlock(e.ignitingBlock ?: return@addCondition false)
        }
        addCondition("cause") { e ->
            asList().any { it.equals(e.cause.name, true) }
        }
    }
}