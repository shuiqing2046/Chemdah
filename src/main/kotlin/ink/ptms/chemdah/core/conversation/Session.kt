package ink.ptms.chemdah.core.conversation

import ink.ptms.chemdah.api.event.collect.ConversationEvents
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.core.conversation.Session
 *
 * @author sky
 * @since 2021/2/9 6:59 下午
 */
class Session(
    var conversation: Conversation,
    var location: Location,
    val origin: Location,
    val player: Player,
    var source: Source<*>,
    val variables: MutableMap<String, Any?> = HashMap(),
) {

    /**
     * 会话是否有效
     */
    @Suppress("KotlinConstantConditions")
    val isValid: Boolean
        get(): Boolean {
            val session = ConversationManager.sessions[player.name]
            return session != null && session === this && session.conversation === conversation
        }

    /**
     * 与会话原点的距离
     */
    val distance: Double
        get() = origin.distance(player.location) - origin.distance(location)

    /** NPC 部分 **/
    val npcSide = ArrayList<String>()
    var npcTalking = false

    /** 玩家部分 **/
    var playerSide: PlayerReply? = null
    var playerReplyForDisplay = ArrayList<PlayerReply>()

    /** 是否跳转 **/
    var isNext = false

    /** 是否关闭 **/
    var isClosed = false

    /** 是否告别 **/
    var isFarewell = false

    /** 是否选择 **/
    var isSelected = false

    /** 开始时间 **/
    val beginTime = System.currentTimeMillis()

    init {
        reload()
    }

    /**
     * 关闭会话
     */
    fun close(refuse: Boolean = false): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        conversation.agent(this, if (refuse) AgentType.REFUSE else AgentType.END).thenApply {
            conversation.option.instanceTheme.onClose(this).thenApply {
                future.complete(null)
                ConversationManager.sessions.remove(player.name)
                ConversationEvents.Closed(this, refuse).call()
            }
        }
        return future
    }

    /**
     * 重置会话
     */
    fun reload() {
        npcSide.clear()
        variables["@Sender"] = player
        isSelected = false
    }

    /**
     * 重置会话展示
     */
    fun resetTheme(): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        conversation.option.instanceTheme.onReset(this).thenApply { future.complete(null) }
        return future
    }
}