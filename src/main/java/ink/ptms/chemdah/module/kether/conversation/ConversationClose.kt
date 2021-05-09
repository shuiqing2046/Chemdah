package ink.ptms.chemdah.module.kether.conversation

import ink.ptms.chemdah.util.getSession
import io.izzel.taboolib.kotlin.kether.KetherParser
import io.izzel.taboolib.kotlin.kether.ScriptParser
import io.izzel.taboolib.kotlin.kether.common.api.QuestAction
import io.izzel.taboolib.kotlin.kether.common.api.QuestContext
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.conversation.ConversationClose
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class ConversationClose : QuestAction<Void>() {

    override fun process(frame: QuestContext.Frame): CompletableFuture<Void> {
        frame.getSession().close()
        return CompletableFuture.completedFuture(null)
    }

    companion object {

        @KetherParser(["close"], namespace = "chemdah-conversation")
        fun parser() = ScriptParser.parser {
            ConversationClose()
        }
    }
}