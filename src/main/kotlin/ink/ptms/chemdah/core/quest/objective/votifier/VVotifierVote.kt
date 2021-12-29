package ink.ptms.chemdah.core.quest.objective.votifier

import com.vexsoftware.votifier.model.VotifierEvent
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.Bukkit

@Dependency("Votifier")
object VVotifierVote : ObjectiveCountableI<VotifierEvent>() {

    override val name = "votifier vote"
    override val event = VotifierEvent::class.java

    init {
        handler {
            Bukkit.getPlayerExact(vote.username)
        }
        addSimpleCondition("position") {
            toPosition().inside(Bukkit.getPlayer(it.vote.username)!!.location)
        }
    }
}
