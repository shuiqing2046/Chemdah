package ink.ptms.chemdah.core.quest.objective.lands

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import me.angeschossen.lands.api.events.LandCreateEvent

@Dependency("Lands")
object LLandsCreate : ObjectiveCountableI<LandCreateEvent>() {

    override val name = "lands create"
    override val event = LandCreateEvent::class.java

    init {
        handler {
            landPlayer.player
        }
        addSimpleCondition("position") {
            toPosition().inside(it.landPlayer.player.location)
        }
    }
}