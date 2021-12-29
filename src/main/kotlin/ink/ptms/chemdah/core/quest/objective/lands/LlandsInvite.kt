package ink.ptms.chemdah.core.quest.objective.lands

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import me.angeschossen.lands.api.events.LandInvitePlayerEvent
import org.bukkit.Bukkit

@Dependency("Lands")
object LlandsInvite : ObjectiveCountableI<LandInvitePlayerEvent>() {

    override val name = "lands invite"
    override val event = LandInvitePlayerEvent::class.java

    init {
        handler {
            Bukkit.getPlayer(it.target)
        }
        addSimpleCondition("position") { data, it ->
            data.toPosition().inside(Bukkit.getPlayer(it.target)!!.location)
        }
    }
}