package ink.ptms.chemdah.core.quest.objective.votifier

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import com.vexsoftware.votifier.model.VotifierEvent
import org.bukkit.Bukkit

@Dependency("Votifier")
object VVotifierVote : ObjectiveCountableI<VotifierEvent>() {

    override val name = "votifier vote"
    override val event = VotifierEvent::class

    init {
        handler {
            Bukkit.getPlayer(vote.username)
        }
        addCondition("position") {
            toPosition().inside(Bukkit.getPlayer(it.vote.username)!!.location)
        }
    }
}
