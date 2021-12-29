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
            it.landPlayer.player
        }
        addSimpleCondition("position") { data, it ->
            data.toPosition().inside(it.landPlayer.player.location)
        }
    }
}