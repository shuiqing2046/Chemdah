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
            it.player
        }
        addSimpleCondition("position") { data, e ->
            data.toPosition().inside(e.harvestedBlock.location)
        }
        addSimpleCondition("material") { data, e ->
            data.toInferBlock().isBlock(e.harvestedBlock)
        }
        addSimpleCondition("item") { data, e ->
            e.itemsHarvested.any { data.toInferItem().isItem(it) }
        }
    }
}