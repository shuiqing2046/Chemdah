package ink.ptms.chemdah.module.kether.conversation

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.core.conversation.Session
import ink.ptms.chemdah.util.rootVariables
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.scriptParser
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.conversation.ConversationGoto
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class ConversationOpen(val conversation: String) : ScriptAction<Session>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Session> {
        val conversation = ChemdahAPI.getConversation(conversation) ?: error("Conversation not found: $conversation")
        val variables = frame.rootVariables()
        val session = variables.get<Session>("@Session").get()
        variables.set("@Cancelled", true)
        return conversation.open(session.player, session.source)
    }

    companion object {

        @KetherParser(["open"], namespace = "chemdah-conversation")
        fun parser() = scriptParser {
            ConversationOpen(it.nextToken())
        }
    }
}