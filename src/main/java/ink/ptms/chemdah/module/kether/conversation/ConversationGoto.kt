package ink.ptms.chemdah.module.kether.conversation

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.core.conversation.Session
import ink.ptms.chemdah.util.getSession
import ink.ptms.chemdah.util.vars
import io.izzel.taboolib.kotlin.kether.KetherParser
import io.izzel.taboolib.kotlin.kether.ScriptParser
import io.izzel.taboolib.kotlin.kether.common.api.QuestAction
import io.izzel.taboolib.kotlin.kether.common.api.QuestContext
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.conversation.ConversationGoto
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class ConversationGoto(val conversation: String) : QuestAction<Session>() {

    override fun process(frame: QuestContext.Frame): CompletableFuture<Session> {
        val conversation = ChemdahAPI.getConversation(conversation) ?: error("Conversation not found: $conversation")
        val session = frame.getSession()
        session.isNext = true
        session.variables.clear()
        session.variables.putAll(frame.vars())
        session.conversation = conversation
        return conversation.open(session.player, session.origin, sessionTop = session)
    }

    companion object {

        @KetherParser(["goto"], namespace = "chemdah-conversation-player")
        fun parser() = ScriptParser.parser {
            ConversationGoto(it.nextToken())
        }
    }
}