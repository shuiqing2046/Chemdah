package ink.ptms.chemdah.module.kether.conversation

import ink.ptms.chemdah.util.getSession
import ink.ptms.chemdah.util.namespaceConversationNPC
import ink.ptms.chemdah.util.vars
import taboolib.module.chat.colored
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.conversation.ConversationTalkNPC
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class ConversationTalkNPC(val token: String) : ScriptAction<Void>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Void> {
        try {
            frame.getSession().npcSide.add(KetherFunction.parse(token, namespace = namespaceConversationNPC) { extend(frame.vars()) }.colored())
        } catch (e: Throwable) {
            e.printKetherErrorMessage()
        }
        return CompletableFuture.completedFuture(null)
    }

    companion object {

        @KetherParser(["talk"], namespace = "chemdah-conversation-npc")
        fun parser() = scriptParser {
            ConversationTalkNPC(it.nextToken())
        }
    }
}