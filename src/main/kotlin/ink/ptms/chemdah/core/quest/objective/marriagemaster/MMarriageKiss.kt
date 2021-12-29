package ink.ptms.chemdah.core.quest.objective.marriagemaster

import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.KissEvent
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI

@Dependency("MarriageMaster")
object MMarriageKiss : ObjectiveCountableI<KissEvent>() {

    override val name = "marriage kiss"
    override val event = KissEvent::class.java

    init {
        handler {
            it.player.playerOnline
        }
        addSimpleCondition("position") { data, it ->
            data.toPosition().inside(it.player.playerOnline!!.location)
        }
    }
}