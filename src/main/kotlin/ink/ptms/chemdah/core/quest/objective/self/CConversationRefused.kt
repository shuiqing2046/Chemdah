package ink.ptms.chemdah.core.quest.objective.self

import ink.ptms.chemdah.api.event.collect.ConversationEvents
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI

/**
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object CConversationRefused : ObjectiveCountableI<ConversationEvents.Closed>() {

    /**
     * 当玩家放弃对话时触发
     */
    override val name = "player conversation refused"
    override val event = ConversationEvents.Closed::class.java

    init {
        handler {
            if (it.refuse) it.session.player else null
        }
        addSimpleCondition("position") { data, it ->
            data.toPosition().inside(it.session.location)
        }
        addSimpleCondition("id") { data, e ->
            data.asList().any { it.equals(e.session.conversation.id, true) }
        }
        addConditionVariable("id") {
            it.session.conversation.id
        }
    }
}