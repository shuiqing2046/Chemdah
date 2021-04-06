package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Abstract
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountable
import org.bukkit.event.player.PlayerBucketEvent
import org.bukkit.inventory.ItemStack

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.APlayerBucket
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Abstract
abstract class APlayerBucket<T : PlayerBucketEvent> : ObjectiveCountable<T>() {

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
            !task.condition.containsKey("material:clicked") || task.condition["material:clicked"]!!.toInferBlock().isBlock(e.blockClicked)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("item") || task.condition["item"]!!.toInferItem().isItem(e.itemStack ?: AIR)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("item:bucket") || task.condition["item:bucket"]!!.toInferItem().isItem(ItemStack(e.bucket))
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("face") || task.condition["face"]!!.asList().any { it.equals(e.blockFace.name, true) }
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("hand") || task.condition["hand"]!!.asList().any { it.equals(e.hand.name, true) }
        }
    }
}