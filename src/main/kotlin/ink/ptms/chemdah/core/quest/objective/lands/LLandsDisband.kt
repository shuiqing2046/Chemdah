package ink.ptms.chemdah.core.quest.objective.lands

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import me.angeschossen.lands.api.events.LandDeleteEvent
import org.bukkit.Bukkit

@Dependency("Lands")
object LLandsDisband : ObjectiveCountableI<LandDeleteEvent>() {

    override val name = "lands disband"
    override val event = LandDeleteEvent::class

    init {
        handler {
            Bukkit.getPlayer(land.ownerUID)
        }
        addSimpleCondition("position") {
            toPosition().inside(Bukkit.getPlayer(it.land.ownerUID)!!.location)
        }
    }
}