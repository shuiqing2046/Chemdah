package ink.ptms.chemdah.module.kether.conversation

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.core.conversation.Session
import ink.ptms.chemdah.util.getSession
import ink.ptms.chemdah.util.vars
import taboolib.module.kether.*
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
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
        val conversation = ChemdahAPI.getConversation(conversation) ?: error("Conversation not found: $conversation")
        val session = frame.getSession()
        session.isNext = true
        session.npcSide.clear()
        session.variables.clear()
        session.variables.putAll(frame.vars())
        session.conversation = conversation
        return conversation.open(session.player, session.origin, sessionTop = session)
    }

    companion object {

        @KetherParser(["goto"], namespace = "chemdah-conversation")
        fun parser() = scriptParser {
            ConversationGoto(it.nextToken())
        }
    }
}