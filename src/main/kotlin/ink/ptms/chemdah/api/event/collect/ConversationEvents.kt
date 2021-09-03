package ink.ptms.chemdah.api.event.collect

import ink.ptms.chemdah.core.conversation.AgentType
import ink.ptms.chemdah.core.conversation.Conversation
import ink.ptms.chemdah.core.conversation.Session
import org.bukkit.inventory.Inventory
import taboolib.platform.type.BukkitProxyEvent

/**
 * Chemdah
 * ink.ptms.chemdah.api.event.collect.ConversationEvents
 *
 * @author sky
 * @since 2021/2/21 1:07 上午
 */
class ConversationEvents {

    /**
     * 当对话中当脚本代理执行时
     */
    class Agent(val conversation: Conversation, val session: Session, val agentType: AgentType): BukkitProxyEvent()

    /**
     * 当对话开始之前
     */
    class Pre(val conversation: Conversation, val session: Session, val relay: Boolean = false): BukkitProxyEvent()

    /**
     * 当对话开始之后
     */
    class Post(val conversation: Conversation, val session: Session, val relay: Boolean = false): BukkitProxyEvent() {

        override val allowCancelled: Boolean
            get() = false
    }

    /**
     * 当对话渲染时
     */
    class Begin(val conversation: Conversation, val session: Session, val relay: Boolean = false): BukkitProxyEvent() {

        override val allowCancelled: Boolean
            get() = false
    }

    /**
     * 当对话被取消时
     */
    class Cancelled(val conversation: Conversation, val session: Session, val relay: Boolean = false): BukkitProxyEvent() {

        override val allowCancelled: Boolean
            get() = false
    }

    /**
     * 当对话结束之后
     */
    class Closed(val session: Session, val refust: Boolean = false): BukkitProxyEvent() {

        override val allowCancelled: Boolean
            get() = false
    }

    /**
     * 当箱子对话页面构建完成后
     */
    class ChestThemeBuild(val session: Session, val message: List<String>, val canReply: Boolean, val inventory: Inventory) : BukkitProxyEvent() {

        override val allowCancelled: Boolean
            get() = false
    }
}