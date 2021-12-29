package ink.ptms.chemdah.core.quest.objective.marriagemaster

import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.HugEvent
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI

@Dependency("MarriageMaster")
object MMarriageHug : ObjectiveCountableI<HugEvent>() {

    override val name = "marriage hug"
    override val event = HugEvent::class.java

    init {
        handler {
            it.player.playerOnline
        }
        addSimpleCondition("position") { data, it ->
            data.toPosition().inside(it.player.playerOnline!!.location)
        }
    }
}