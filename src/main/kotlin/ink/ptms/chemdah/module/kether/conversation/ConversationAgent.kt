package ink.ptms.chemdah.module.kether.conversation

import ink.ptms.chemdah.util.getSession
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.conversation.ConversationAgent
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class ConversationAgent : ScriptAction<Void>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Void> {
        val session = frame.getSession()
        session.variables.clear()
        session.variables.putAll(frame.deepVars().filterKeys { !it.startsWith("~") })
        return CompletableFuture.completedFuture(null)
    }

    companion object {

        @KetherParser(["agent"], namespace = "chemdah-conversation")
        fun parser() = scriptParser {
            ConversationAgent()
        }
    }
}