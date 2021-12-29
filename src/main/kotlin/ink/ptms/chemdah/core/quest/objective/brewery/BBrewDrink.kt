package ink.ptms.chemdah.core.quest.objective.brewery

import com.dre.brewery.api.events.brew.BrewDrinkEvent
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objiective.brewery.BBrewDrink
 *
 * @author Peng_Lx
 * @since 2021/6/4 23:02 下午
 */
@Dependency("Brewery")
object BBrewDrink : ObjectiveCountableI<BrewDrinkEvent>() {

    override val name = "brewery drink"
    override val event = BrewDrinkEvent::class.java

    init {
        handler {
            it.player
        }
        addSimpleCondition("position") { data, it ->
            data.toPosition().inside(it.player.location)
        }
        addSimpleCondition("brew") { data, it ->
            data.toInferItem().isItem(ItemStack(Material.POTION).also { item -> item.itemMeta = it.itemMeta })
        }
        addSimpleCondition("alcohol") { data, it ->
            data.toInt() <= it.addedAlcohol
        }
        addSimpleCondition("quality") { data, it ->
            data.toInt() <= it.quality
        }
        addConditionVariable("alcohol") {
            it.addedAlcohol
        }
        addConditionVariable("quality") {
            it.quality
        }
    }
}