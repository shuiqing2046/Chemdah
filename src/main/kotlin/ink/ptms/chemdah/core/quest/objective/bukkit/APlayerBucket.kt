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
            it.player
        }
        addSimpleCondition("position") { data, it ->
            data.toPosition().inside(it.blockClicked.location)
        }
        addSimpleCondition("material") { data, it ->
            data.toInferBlock().isBlock(it.blockClicked)
        }
        addSimpleCondition("material:clicked") { data, it ->
            data.toInferBlock().isBlock(it.blockClicked)
        }
        addSimpleCondition("item") { data, it ->
            data.toInferItem().isItem(it.itemStack ?: EMPTY_ITEM)
        }
        addSimpleCondition("item:bucket") { data, it ->
            data.toInferItem().isItem(ItemStack(it.bucket))
        }
        addSimpleCondition("face") { data, e ->
            data.asList().any { it.equals(e.blockFace.name, true) }
        }
        addSimpleCondition("hand") { data, e ->
            data.asList().any { it.equals(e.invokeMethod<Any>("getHand").toString(), true) }
        }
    }
}