package ink.ptms.chemdah.core.quest.objective.brewery

import com.dre.brewery.api.events.brew.BrewDrinkEvent
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI

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
    override val event = BrewDrinkEvent::class

    init {
        handler {
            player
        }
        addCondition("alcohol") {
            toInt() <= it.addedAlcohol
        }
        addCondition("quality") {
            toInt() <= it.quality
        }
        addCondition("position") {
            toPosition().inside(it.player.location)
        }
    }
}