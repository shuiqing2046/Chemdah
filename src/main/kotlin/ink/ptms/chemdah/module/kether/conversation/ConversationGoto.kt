package ink.ptms.chemdah.module.kether.conversation

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.core.conversation.ConversationSwitch
import ink.ptms.chemdah.core.conversation.Session
import ink.ptms.chemdah.util.getSession
import ink.ptms.chemdah.util.vars
import org.bukkit.entity.Player
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.conversation.ConversationGoto
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class ConversationGoto(val conversation: String) : ScriptAction<Session>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Session> {
        val session = frame.getSession()
        val conversation = ChemdahAPI.getConversation(conversation)
        if (conversation != null) {
            session.goto(conversation, frame.vars())
            return conversation.open(session.player, session.source, session)
        }
        val switchTo = ConversationSwitch.switchMap[this.conversation]
        if (switchTo != null) {
            val future = CompletableFuture<Session>()
            val player = frame.player().castSafely<Player>() ?: error("No player selected.")
            switchTo.get(player).thenAccept { case ->
                val find = case.open(player)
                if (find != null) {
                    session.goto(find, frame.vars())
                    find.open(session.player, session.source, session).thenAccept { future.complete(it) }
                } else {
                    future.complete(session)
                }
            }
            return future
        }
        error("Conversation not found: ${this.conversation}")
    }

    companion object {

        @KetherParser(["goto"], namespace = "chemdah-conversation")
        fun parser() = scriptParser {
            ConversationGoto(it.nextToken())
        }
    }
}