package ink.ptms.chemdah.core.quest.objective.self

import ink.ptms.chemdah.api.event.collect.ConversationEvents
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI

/**
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object CConversationReply : ObjectiveCountableI<ConversationEvents.ReplyClosed>() {

    /**
     * 当玩家通过正常途径结束对话时触发
     */
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
        addConditionVariable("id") {
            it.session.conversation.id
        }
    }
}