package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Task
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountable
import org.bukkit.event.enchantment.EnchantItemEvent
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.event.inventory.FurnaceExtractEvent
import org.bukkit.event.inventory.FurnaceSmeltEvent
import org.bukkit.inventory.ItemStack

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IItemFurnace
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IItemFurnace : ObjectiveCountable<FurnaceExtractEvent>() {

    override val name = "item furnace"
    override val event = FurnaceExtractEvent::class

    init {
        handler {
            player
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("position") || task.condition["position"]!!.toPosition().inside(e.block.location)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("item") || task.condition["item"]!!.toItem().match(ItemStack(e.itemType))
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("exp") || task.condition["exp"]!!.toInt() <= e.expToDrop
        }
    }

    override fun getCount(profile: PlayerProfile, task: Task, event: FurnaceExtractEvent): Int {
        return event.itemAmount
    }
}