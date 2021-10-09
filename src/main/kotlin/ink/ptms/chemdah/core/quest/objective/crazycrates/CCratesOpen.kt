package ink.ptms.chemdah.core.quest.objective.crazycrates

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import me.badbones69.crazycrates.api.events.PlayerPrizeEvent

@Dependency("CrayzCrates")
object CCratesOpen : ObjectiveCountableI<PlayerPrizeEvent>() {

    override val name = "cc open"
    override val event = PlayerPrizeEvent::class

    init {
        handler {
            player
        }
        addCondition("position") { e ->
            toPosition().inside(e.player.location)
        }
        addCondition("name") { e ->
            asList().any { it.equals(e.crate.name, true) }
        }
        addConditionVariable("name") {
            it.crate.name
        }
    }
}