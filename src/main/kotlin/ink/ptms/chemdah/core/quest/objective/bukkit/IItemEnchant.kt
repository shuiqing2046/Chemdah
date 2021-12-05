package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.event.enchantment.EnchantItemEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IItemEnchant
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IItemEnchant : ObjectiveCountableI<EnchantItemEvent>() {

    override val name = "enchant item"
    override val event = EnchantItemEvent::class

    init {
        handler {
            enchanter
        }
        addSimpleCondition("position") { e ->
            toPosition().inside(e.enchantBlock.location)
        }
        addSimpleCondition("item") { e ->
            toInferItem().isItem(e.item)
        }
        addSimpleCondition("type") { e ->
            asList().any { e.enchantsToAdd.any { e -> e.key.name.equals(it, true) } }
        }
        addSimpleCondition("cost") { e ->
            toInt() <= e.expLevelCost
        }
        addConditionVariable("cost") {
            it.expLevelCost
        }
    }
}