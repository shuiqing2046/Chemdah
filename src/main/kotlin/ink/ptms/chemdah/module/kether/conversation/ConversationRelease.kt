package ink.ptms.chemdah.module.kether.conversation

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.core.conversation.theme.ThemeChatSettings
import ink.ptms.chemdah.core.quest.QuestDevelopment.releaseTransmit
import ink.ptms.chemdah.util.getBukkitPlayer
import taboolib.common.platform.function.adaptCommandSender
import taboolib.module.chat.TellrawJson
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
class ConversationRelease() : ScriptAction<Void>() {

    private val settings = ChemdahAPI.getConversationTheme("chat")!!.settings as ThemeChatSettings

    private fun newJson() = TellrawJson().also { json -> repeat(settings.spaceLine) { json.newLine() } }.fixed()

    private fun TellrawJson.fixed(): TellrawJson {
        return append("\n").runCommand("PLEASE!PASS!ME!d3486345-e35d-326a-b5c5-787de3814770!")
    }

    override fun run(frame: ScriptFrame): CompletableFuture<Void> {
        newJson().sendTo(adaptCommandSender(frame.getBukkitPlayer()))
        frame.getBukkitPlayer().releaseTransmit()
        return CompletableFuture.completedFuture(null)
    }

    companion object {

        @KetherParser(["release"], namespace = "chemdah-conversation-player")
        fun parser() = scriptParser {
            ConversationRelease()
        }
    }
}