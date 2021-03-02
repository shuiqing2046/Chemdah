package ink.ptms.chemdah.core.conversation

import ink.ptms.chemdah.Chemdah
import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.api.event.ConversationEvents
import io.izzel.taboolib.module.command.lite.CommandBuilder
import io.izzel.taboolib.module.inject.TInject
import io.izzel.taboolib.module.inject.TListener
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.permissions.PermissionDefault
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.core.conversation.TalkSession
 *
 * @author sky
 * @since 2021/2/9 6:59 下午
 */
data class Session(
    var conversation: Conversation,
    var location: Location,
    val origin: Location,
    val player: Player,
    val variables: MutableMap<String, Any?> = HashMap()
) {

    val isValid: Boolean
        get(): Boolean {
            val session = ConversationManager.sessions[player.name]
            return session != null && session === this && session.conversation === conversation
        }

    val distance: Double
        get() = origin.distance(player.location) - origin.distance(location)

    val npcSide = ArrayList<String>()
    var npcName = ""
    var npcTalking = false

    var playerSide: PlayerReply? = null
    var playerReplyForDisplay = ArrayList<PlayerReply>()

    var isNext = false
    var isClosed = false

    /**
     * 关闭会话
     */
    fun close(refuse: Boolean = false): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        conversation.agent(this, if (refuse) AgentType.REFUSE else AgentType.END).thenApply {
            conversation.option.instanceTheme.end(this).thenApply {
                future.complete(null)
                ConversationManager.sessions.remove(player.name)
                ConversationEvents.Closed(this).call()
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
    }

    /**
     * 重置会话展示
     */
    fun reloadTheme(): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        conversation.option.instanceTheme.reload(this).thenApply {
            future.complete(null)
        }
        return future
    }

    init {
        reload()
    }
}