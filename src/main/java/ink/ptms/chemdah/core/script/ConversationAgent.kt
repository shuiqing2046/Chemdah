package ink.ptms.chemdah.core.script

import io.izzel.taboolib.kotlin.kether.KetherParser
import io.izzel.taboolib.kotlin.kether.ScriptParser
import io.izzel.taboolib.kotlin.kether.common.api.QuestAction
import io.izzel.taboolib.kotlin.kether.common.api.QuestContext
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.core.script.ConversationAgent
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class ConversationAgent : QuestAction<Void>() {

    override fun process(frame: QuestContext.Frame): CompletableFuture<Void> {
        val session = frame.getSession()
        session.variables.clear()
        session.variables.putAll(frame.vars())
        return CompletableFuture.completedFuture(null)
    }

    companion object {

        @KetherParser(["agent"], namespace = "chemdah:conversation")
        fun parser() = ScriptParser.parser {
            ConversationAgent()
        }
    }
}