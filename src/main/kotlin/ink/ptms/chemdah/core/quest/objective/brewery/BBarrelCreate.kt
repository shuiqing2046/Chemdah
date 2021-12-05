package ink.ptms.chemdah.core.quest.objective.brewery

import com.dre.brewery.api.events.barrel.BarrelCreateEvent
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objiective.brewery.BBarrelCreate
 *
 * @author Peng_Lx
 * @since 2021/6/4 23:02 下午
 */
@Dependency("Brewery")
object BBarrelCreate : ObjectiveCountableI<BarrelCreateEvent>() {

    override val name = "brewery barrel create"
    override val event = BarrelCreateEvent::class

    init {
        handler {
            player
        }
        addSimpleCondition("position") {
            toPosition().inside(it.barrel.spigot.location)
        }
    }
}