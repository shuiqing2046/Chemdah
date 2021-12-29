package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.api.event.collect.ConversationEvents
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerAttack
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerConversation : ObjectiveCountableI<ConversationEvents.ReplyClosed>() {

    override val name = "player conversation"
    override val event = ConversationEvents.ReplyClosed::class.java
    override val isAsync = true

    init {
        handler {
            it.session.player
        }
        addSimpleCondition("position") { data, e ->
            data.toPosition().inside(e.session.origin)
        }
        addSimpleCondition("id") { data, e ->
            data.asList().any { it.equals(e.session.conversation.id, true) }
        }
    }
}