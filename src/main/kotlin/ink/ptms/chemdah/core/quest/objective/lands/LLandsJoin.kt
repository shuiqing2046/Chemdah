package ink.ptms.chemdah.core.quest.objective.lands

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import me.angeschossen.lands.api.events.LandTrustPlayerEvent
import org.bukkit.Bukkit

@Dependency("Lands")
object LLandsJoin : ObjectiveCountableI<LandTrustPlayerEvent>() {

    override val name = "lands join"
    override val event = LandTrustPlayerEvent::class.java

    init {
        handler {
            Bukkit.getPlayer(target)
        }
        addSimpleCondition("position") {
            toPosition().inside(Bukkit.getPlayer(it.target)!!.location)
        }
    }
}