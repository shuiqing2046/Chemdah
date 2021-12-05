package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Abstract
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.event.player.PlayerBucketEvent
import org.bukkit.inventory.ItemStack
import taboolib.common.reflect.Reflex.Companion.invokeMethod

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.APlayerBucket
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Abstract
abstract class APlayerBucket<T : PlayerBucketEvent> : ObjectiveCountableI<T>() {

    init {
        handler {
            player
        }
        addSimpleCondition("position") {
            toPosition().inside(it.block.location)
        }
        addSimpleCondition("material") {
            toInferBlock().isBlock(it.block)
        }
        addSimpleCondition("material:clicked") {
            toInferBlock().isBlock(it.blockClicked)
        }
        addSimpleCondition("item") {
            toInferItem().isItem(it.itemStack ?: AIR)
        }
        addSimpleCondition("item:bucket") {
            toInferItem().isItem(ItemStack(it.bucket))
        }
        addSimpleCondition("face") { e ->
            asList().any { it.equals(e.blockFace.name, true) }
        }
        addSimpleCondition("hand") { e ->
            asList().any { it.equals(e.invokeMethod<Any>("getHand").toString(), true) }
        }
    }
}