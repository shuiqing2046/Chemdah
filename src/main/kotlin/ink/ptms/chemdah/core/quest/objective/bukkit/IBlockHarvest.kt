package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.event.player.PlayerHarvestBlockEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IBlockHarvest
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IBlockHarvest : ObjectiveCountableI<PlayerHarvestBlockEvent>() {

    override val name = "harvest block"
    override val event = PlayerHarvestBlockEvent::class.java

    init {
        handler {
            player
        }
        addSimpleCondition("position") { e ->
            toPosition().inside(e.harvestedBlock.location)
        }
        addSimpleCondition("material") { e ->
            toInferBlock().isBlock(e.harvestedBlock)
        }
        addSimpleCondition("item") { e ->
            e.itemsHarvested.any { toInferItem().isItem(it) }
        }
    }
}