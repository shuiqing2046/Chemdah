package ink.ptms.chemdah.api.event

import ink.ptms.chemdah.core.conversation.AgentType
import ink.ptms.chemdah.core.conversation.Conversation
import ink.ptms.chemdah.core.conversation.Session
import io.izzel.taboolib.module.event.EventCancellable
import io.izzel.taboolib.module.event.EventNormal
import org.bukkit.Bukkit

/**
 * Chemdah
 * ink.ptms.chemdah.api.event.ConversationEvent
 *
 * @author sky
 * @since 2021/2/21 1:07 上午
 */
class ConversationEvent {

    /**
     * 当对话中当脚本代理执行时
     */
    class Agent(val conversation: Conversation, val session: Session, val agentType: AgentType): EventCancellable<Agent>() {

        init {
            async(!Bukkit.isPrimaryThread())
        }
    }

    /**
     * 当对话开始之前
     */
    class Pre(val conversation: Conversation, val session: Session, val relay: Boolean = false): EventCancellable<Pre>() {

        init {
            async(!Bukkit.isPrimaryThread())
        }
    }

    /**
     * 当对话开始之后
     */
    class Post(val conversation: Conversation, val session: Session, val relay: Boolean = false): EventNormal<Post>() {

        init {
            async(!Bukkit.isPrimaryThread())
        }
    }

    /**
     * 当对话渲染时
     */
    class Begin(val conversation: Conversation, val session: Session, val relay: Boolean = false): EventNormal<Begin>() {

        init {
            async(!Bukkit.isPrimaryThread())
        }
    }

    /**
     * 当对话被取消时
     */
    class Cancelled(val conversation: Conversation, val session: Session, val relay: Boolean = false): EventNormal<Cancelled>() {

        init {
            async(!Bukkit.isPrimaryThread())
        }
    }

    /**
     * 当对话结束之后
     */
    class Closed(val session: Session): EventNormal<Closed>() {

        init {
            async(!Bukkit.isPrimaryThread())
        }
    }
}