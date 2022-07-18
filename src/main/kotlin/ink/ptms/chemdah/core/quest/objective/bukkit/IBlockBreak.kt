package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.QuestDevelopment.isPlaced
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.enchantments.Enchantment
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
    override val event = BlockBreakEvent::class.java
    override val isAsync = true

    init {
        handler {
            it.player
        }
        addSimpleCondition("position") { data, it ->
            data.toPosition().inside(it.block.location)
        }
        addSimpleCondition("material") { data, it ->
            data.toInferBlock().isBlock(it.block)
        }
        addSimpleCondition("exp") { data, it ->
            data.toInt() <= it.expToDrop
        }
        addSimpleCondition("unique") { data, it ->
            data.toBoolean() == it.block.isPlaced()
        }
        addSimpleCondition("no-silk-touch") { data, it ->
            if (it.player.inventory.itemInMainHand.itemMeta?.hasEnchant(Enchantment.SILK_TOUCH) == true) data.toBoolean() else true
        }
        addConditionVariable("exp") {
            it.expToDrop
        }
    }
}