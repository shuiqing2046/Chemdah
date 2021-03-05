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
 * ink.ptms.chemdah.core.conversation.kether.ConversationTalkNPC
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class ConversationTalkNPC(val token: String) : QuestAction<Void>() {

    override fun process(frame: QuestContext.Frame): CompletableFuture<Void> {
        try {
            frame.getSession().npcSide.add(KetherFunction.parse(token, namespace = namespaceConversationNPC) { extend(frame.vars()) }.colored())
        } catch (e: Throwable) {
            e.print()
        }
        return CompletableFuture.completedFuture(null)
    }

    companion object {

        @KetherParser(["talk"], namespace = "chemdah-conversation-npc")
        fun parser() = ScriptParser.parser {
            ConversationTalkNPC(it.nextToken())
        }
    }
}