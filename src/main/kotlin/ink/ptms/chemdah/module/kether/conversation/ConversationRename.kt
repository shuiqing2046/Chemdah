package ink.ptms.chemdah.module.kether.conversation

import ink.ptms.chemdah.core.conversation.Session
import ink.ptms.chemdah.util.rootVariables
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
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
class ConversationRename(val rename: ParsedAction<*>) : ScriptAction<Void>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Void> {
        return frame.newFrame(rename).run<Any>().thenAccept {
            frame.rootVariables().get<Session>("@Session").get().source.name = it.toString()
        }
    }

    companion object {

        @KetherParser(["rename"], namespace = "chemdah-conversation")
        fun parser() = scriptParser {
            ConversationRename(it.next(ArgTypes.ACTION))
        }
    }
}