package ink.ptms.chemdah.module.kether.conversation

import ink.ptms.chemdah.util.getSession
import taboolib.module.kether.*
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.conversation.ConversationClose
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class ConversationClose : ScriptAction<Void>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Void> {
        frame.getSession().close()
        return CompletableFuture.completedFuture(null)
    }

    companion object {

        @KetherParser(["close"], namespace = "chemdah-conversation")
        fun parser() = scriptParser {
            ConversationClose()
        }
    }
}