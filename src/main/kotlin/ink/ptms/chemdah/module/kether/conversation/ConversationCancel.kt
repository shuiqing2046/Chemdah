package ink.ptms.chemdah.module.kether.conversation

import ink.ptms.chemdah.util.rootVariables
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.conversation.ConversationCancel
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class ConversationCancel : ScriptAction<Void>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Void> {
        frame.rootVariables().set("@Cancelled", true)
        return CompletableFuture.completedFuture(null)
    }

    companion object {

        @KetherParser(["cancel"], namespace = "chemdah-conversation")
        fun parser() = scriptParser {
            ConversationCancel()
        }
    }
}