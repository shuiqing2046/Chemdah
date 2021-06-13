package ink.ptms.chemdah.module.kether.conversation

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.core.conversation.theme.ThemeChatSettings
import ink.ptms.chemdah.core.quest.QuestDevelopment.releaseTransmit
import ink.ptms.chemdah.util.getPlayer
import io.izzel.taboolib.kotlin.kether.KetherParser
import io.izzel.taboolib.kotlin.kether.ScriptParser
import io.izzel.taboolib.kotlin.kether.common.api.QuestAction
import io.izzel.taboolib.kotlin.kether.common.api.QuestContext
import io.izzel.taboolib.module.tellraw.TellrawJson
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.conversation.ConversationGoto
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class ConversationRelease() : QuestAction<Void>() {

    private val settings = ChemdahAPI.getConversationTheme("chat")!!.settings as ThemeChatSettings

    private fun newJson() = TellrawJson.create().also { json -> repeat(settings.spaceLine) { json.newLine() } }.fixed()

    private fun TellrawJson.fixed(): TellrawJson {
        return append("\n").clickCommand("PLEASE!PASS!ME!d3486345-e35d-326a-b5c5-787de3814770!")
    }

    override fun process(frame: QuestContext.Frame): CompletableFuture<Void> {
        newJson().send(frame.getPlayer())
        frame.getPlayer().releaseTransmit()
        return CompletableFuture.completedFuture(null)
    }

    override fun toString(): String {
        return "ConversationRelease()"
    }

    companion object {

        @KetherParser(["release"], namespace = "chemdah-conversation-player")
        fun parser() = ScriptParser.parser {
            ConversationRelease()
        }
    }
}