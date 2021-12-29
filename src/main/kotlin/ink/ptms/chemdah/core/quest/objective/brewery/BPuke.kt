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
    override val event = PlayerPukeEvent::class.java

    init {
        handler {
            it.player
        }
        addSimpleCondition("position") { data, it ->
            data.toPosition().inside(it.player.location)
        }
        addSimpleCondition("count") { data, it ->
            data.toInt() <= it.count
        }
        addConditionVariable("count") {
            it.count
        }
    }
}