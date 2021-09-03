package ink.ptms.chemdah.core.quest.objective.brewery

import com.dre.brewery.api.events.PlayerPukeEvent
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objiective.brewery.BPuke
 *
 * @author Peng_Lx
 * @since 2021/6/4 23:02 下午
 */
@Dependency("Brewery")
object BPuke : ObjectiveCountableI<PlayerPukeEvent>() {

    override val name = "brewery puke"
    override val event = PlayerPukeEvent::class

    init {
        handler {
            player
        }
        addCondition("position") {
            toPosition().inside(it.player.location)
        }
        addCondition("count") {
            toInt() <= it.count
        }
        addConditionVariable("count") {
            it.count
        }
    }
}