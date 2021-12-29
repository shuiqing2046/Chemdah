package ink.ptms.chemdah.core.quest.objective.chatreaction

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import me.clip.chatreaction.events.ReactionWinEvent

@Dependency("ChatReaction")
object CChatReactionWin : ObjectiveCountableI<ReactionWinEvent>() {

    override val name = "chatreaction win"
    override val event = ReactionWinEvent::class.java

    init {
        handler {
            it.winner
        }
        addSimpleCondition("position") { data, it ->
            data.toPosition().inside(it.winner.location)
        }
    }
}