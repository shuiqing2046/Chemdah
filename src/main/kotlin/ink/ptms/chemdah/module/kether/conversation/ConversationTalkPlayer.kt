package ink.ptms.chemdah.module.kether.conversation

import ink.ptms.adyeshach.api.AdyeshachAPI
import ink.ptms.chemdah.util.getSession
import ink.ptms.chemdah.util.namespaceConversationPlayer
import ink.ptms.chemdah.util.vars
import taboolib.module.chat.colored
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.conversation.ConversationTalkPlayer
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class ConversationTalkPlayer(val token: String) : ScriptAction<Void>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Void> {
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
                        AdyeshachAPI.createHolographic(session.player, session.origin.clone().add(0.0, 0.25 + (index * 0.3), 0.0), 40, { it }, "&7$s")
                    }
                }
            }
        } catch (e: Throwable) {
            e.printKetherErrorMessage()
        }
        return CompletableFuture.completedFuture(null)
    }

    override fun toString(): String {
        return "ConversationTalkPlayer(token='$token')"
    }

    companion object {

        @KetherParser(["talk"], namespace = "chemdah-conversation-player")
        fun parser() = scriptParser {
            ConversationTalkPlayer(it.nextToken())
        }
    }
}