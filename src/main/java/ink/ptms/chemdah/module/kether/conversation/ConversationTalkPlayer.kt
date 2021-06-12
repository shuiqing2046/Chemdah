package ink.ptms.chemdah.module.kether.conversation

import ink.ptms.chemdah.util.*
import io.izzel.taboolib.kotlin.kether.KetherFunction
import io.izzel.taboolib.kotlin.kether.KetherParser
import io.izzel.taboolib.kotlin.kether.ScriptParser
import io.izzel.taboolib.kotlin.kether.common.api.QuestAction
import io.izzel.taboolib.kotlin.kether.common.api.QuestContext
import io.izzel.taboolib.kotlin.sendHolographic
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.conversation.ConversationTalkPlayer
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class ConversationTalkPlayer(val token: String) : QuestAction<Void>() {

    override fun process(frame: QuestContext.Frame): CompletableFuture<Void> {
        val session = frame.getSession()
        try {
            KetherFunction.parse(token, namespace = namespaceConversationPlayer) { extend(frame.vars()) }.run {
                val messages = split("\\n").colored()
                val theme = session.conversation.option.instanceTheme
                if (theme.allowFarewell()) {
                    session.npcSide.clear()
                    session.npcSide.addAll(messages)
                    session.isFarewell = true
                    return theme.onDisplay(session, messages, false)
                } else {
                    theme.settings.playSound(session)
                    messages.forEachIndexed { index, s ->
                        session.player.sendHolographic(session.origin.clone().add(0.0, 0.25 + (index * 0.3), 0.0), "&7$s")
                    }
                }
            }
        } catch (e: Throwable) {
            e.print()
        }
        return CompletableFuture.completedFuture(null)
    }

    override fun toString(): String {
        return "ConversationTalkPlayer(token='$token')"
    }

    companion object {

        @KetherParser(["talk"], namespace = "chemdah-conversation-player")
        fun parser() = ScriptParser.parser {
            ConversationTalkPlayer(it.nextToken())
        }
    }
}