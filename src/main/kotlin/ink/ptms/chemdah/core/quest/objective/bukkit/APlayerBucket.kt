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
        addCondition("position") {
            toPosition().inside(it.block.location)
        }
        addCondition("material") {
            toInferBlock().isBlock(it.block)
        }
        addCondition("material:clicked") {
            toInferBlock().isBlock(it.blockClicked)
        }
        addCondition("item") {
            toInferItem().isItem(it.itemStack ?: AIR)
        }
        addCondition("item:bucket") {
            toInferItem().isItem(ItemStack(it.bucket))
        }
        addCondition("face") { e ->
            asList().any { it.equals(e.blockFace.name, true) }
        }
        addCondition("hand") { e ->
            asList().any { it.equals(e.invokeMethod<Any>("getHand").toString(), true) }
        }
    }
}