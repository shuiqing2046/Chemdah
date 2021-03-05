package ink.ptms.chemdah.core.conversation.kether

import ink.ptms.chemdah.util.*
import io.izzel.taboolib.kotlin.kether.KetherFunction
import io.izzel.taboolib.kotlin.kether.KetherParser
import io.izzel.taboolib.kotlin.kether.ScriptParser
import io.izzel.taboolib.kotlin.kether.common.api.QuestAction
import io.izzel.taboolib.kotlin.kether.common.api.QuestContext
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.core.conversation.kether.ConversationTalkPlayer
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class ConversationTalkPlayer(val token: String) : QuestAction<Void>() {

    override fun process(frame: QuestContext.Frame): CompletableFuture<Void> {
        return try {
            KetherFunction.parse(token, namespace = namespaceConversationPlayer) { extend(frame.vars()) }.run {
                val session = frame.getSession()
                session.conversation.option.instanceTheme.npcTalk(session, listOf(colored()), false)
            }
        } catch (e: Throwable) {
            e.print()
            CompletableFuture.completedFuture(null)
        }
    }

    companion object {

        @KetherParser(["talk"], namespace = "chemdah-conversation-player")
        fun parser() = ScriptParser.parser {
            ConversationTalkPlayer(it.nextToken())
        }
    }
}