package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountable
import org.bukkit.event.enchantment.EnchantItemEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IItemEnchant
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IItemEnchant : ObjectiveCountable<EnchantItemEvent>() {

    override val name = "enchant item"
    override val event = EnchantItemEvent::class

    init {
        handler {
            enchanter
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("position") || task.condition["position"]!!.toPosition().inside(e.enchantBlock.location)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("item") || task.condition["item"]!!.toInferItem().isItem(e.item)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("type") || task.condition["type"]!!.asList().any { e.enchantsToAdd.any { e -> e.key.name.equals(it, true) } }
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("cost") || task.condition["cost"]!!.toInt() <= e.expLevelCost
        }
    }
}