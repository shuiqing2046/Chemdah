package ink.ptms.chemdah.core.quest.objective.lands

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import me.angeschossen.lands.api.events.LandUntrustPlayerEvent
import org.bukkit.Bukkit

@Dependency("Lands")
object LLandsLeave : ObjectiveCountableI<LandUntrustPlayerEvent>() {

    override val name = "lands leave"
    override val event = LandUntrustPlayerEvent::class.java

    init {
        handler {
            Bukkit.getPlayer(it.target)
        }
        addSimpleCondition("position") { data, it ->
            data.toPosition().inside(Bukkit.getPlayer(it.target)!!.location)
        }
    }
}