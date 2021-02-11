package ink.ptms.chemdah.core.script

import io.izzel.taboolib.kotlin.kether.KetherFunction
import io.izzel.taboolib.kotlin.kether.KetherParser
import io.izzel.taboolib.kotlin.kether.ScriptParser
import io.izzel.taboolib.kotlin.kether.common.api.QuestAction
import io.izzel.taboolib.kotlin.kether.common.api.QuestContext
import io.izzel.taboolib.kotlin.kether.common.util.LocalizedException
import java.util.concurrent.CompletableFuture

class ConversationTalkPlayer(val token: String) : QuestAction<Void>() {

    override fun process(frame: QuestContext.Frame): CompletableFuture<Void> {
        return try {
            KetherFunction.parse(token, namespace = namespaceConversationPlayer) { extend(frame.vars()) }.run {
                val session = frame.getSession()
                session.conversation.option.instanceTheme.npcTalk(session, listOf(colored()), false)
            }
        } catch (e: LocalizedException) {
            e.print()
            CompletableFuture.completedFuture(null)
        } catch (e: Throwable) {
            e.printStackTrace()
            CompletableFuture.completedFuture(null)
        }
    }

    companion object {

        @KetherParser(["talk"], namespace = "chemdah:conversation:player")
        fun parser() = ScriptParser.parser {
            ConversationTalkPlayer(it.nextToken())
        }
    }
}