package ink.ptms.chemdah.core.quest.objective.marriagemaster

import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.MarryEvent
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI

@Dependency("MarriageMaster")
object MMarriageMarried : ObjectiveCountableI<MarryEvent>() {

    override val name = "marriage marry"
    override val event = MarryEvent::class.java

    init {
        handler {
            it.player1.playerOnline
        }
        addSimpleCondition("position") { data, it ->
            data.toPosition().inside(it.player1.playerOnline!!.location) && data.toPosition().inside(it.player2.playerOnline!!.location)
        }
    }

}