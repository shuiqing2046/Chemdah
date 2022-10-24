package ink.ptms.chemdah.api.event.collect

import ink.ptms.chemdah.core.conversation.*
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import taboolib.library.configuration.ConfigurationSection
import taboolib.platform.type.BukkitProxyEvent
import java.io.File

/**
 * Chemdah
 * ink.ptms.chemdah.api.event.collect.ConversationEvents
 *
 * @author sky
 * @since 2021/2/21 1:07 上午
 */
class ConversationEvents {

    /**
     * 当对话被加载
     */
    class Load(val file: File?, val option: Option, val root: ConfigurationSection): BukkitProxyEvent()

    /**
     * 当玩家选择对话时（对话开始之前）
     */
    class Select(val player: Player, val namespace: String, val id: List<String>, var conversation: Conversation?): BukkitProxyEvent()

    /**
     * 当玩家选择对话回复时
     */
    class SelectReply(val player: Player, val session: Session, val reply: PlayerReply): BukkitProxyEvent()

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
    class Closed(val session: Session, val refuse: Boolean = false): BukkitProxyEvent() {

        override val allowCancelled: Boolean
            get() = false
    }

    /**
     * 当玩家通过正常途径（回复）结束对话时
     * 该事件在 Closed 事件之前触发
     */
    class ReplyClosed(val session: Session): BukkitProxyEvent() {

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