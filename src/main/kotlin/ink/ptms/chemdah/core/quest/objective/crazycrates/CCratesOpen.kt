package ink.ptms.chemdah.core.quest.objective.crazycrates

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import me.badbones69.crazycrates.api.events.PlayerPrizeEvent

@Dependency("CrayzCrates")
object CCratesOpen : ObjectiveCountableI<PlayerPrizeEvent>() {

    override val name = "cc open"
    override val event = PlayerPrizeEvent::class.java

    init {
        handler {
            it.player
        }
        addSimpleCondition("position") { data, e ->
            data.toPosition().inside(e.player.location)
        }
        addSimpleCondition("name") { data, e ->
            data.asList().any { it.equals(e.crate.name, true) }
        }
        addConditionVariable("name") {
            it.crate.name
        }
    }
}