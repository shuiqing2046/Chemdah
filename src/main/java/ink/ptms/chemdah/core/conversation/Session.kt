package ink.ptms.chemdah.core.conversation

import ink.ptms.chemdah.Chemdah
import ink.ptms.chemdah.api.ChemdahAPI
import io.izzel.taboolib.module.command.lite.CommandBuilder
import io.izzel.taboolib.module.inject.TInject
import org.bukkit.Location
import org.bukkit.entity.Player
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

    val npcSide = ArrayList<String>()
    var npcTalking = false

    var playerSide: PlayerReply? = null
    var playerReplyForDisplay = ArrayList<PlayerReply>()

    var next = false
    var refuse = 0

    /**
     * 关闭会话
     */
    fun close(refuse: Boolean = false): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        conversation.agent(this, if (refuse) AgentType.REFUSE else AgentType.END).thenApply {
            conversation.option.instanceTheme.end(this).thenApply {
                future.complete(null)
                ConversationManager.sessions.remove(player.name)
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

    companion object {

        @TInject
        val session = CommandBuilder.create("session", Chemdah.plugin)
            .permissionDefault(PermissionDefault.TRUE)
            .execute { sender, args ->
                if (sender is Player) {
                    if (args.getOrNull(0) == "reply" && args.size == 2) {
                        val session = ChemdahAPI.getConversationSession(sender) ?: return@execute
                        session.conversation.playerSide.checked(session).thenApply { replies ->
                            replies.firstOrNull { it.uuid.toString() == args[1] }?.select(session)
                        }
                    }
                }
            }!!
    }
}