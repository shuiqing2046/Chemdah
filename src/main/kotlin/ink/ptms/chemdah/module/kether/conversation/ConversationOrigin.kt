package ink.ptms.chemdah.module.kether.conversation

import ink.ptms.chemdah.util.getSession
import org.bukkit.Location
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.scriptParser
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.ConversationLocation
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class ConversationOrigin : ScriptAction<Location>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Location> {
        return CompletableFuture.completedFuture(frame.getSession().origin)
    }

    companion object {

        @KetherParser(["origin"], namespace = "chemdah-conversation")
        fun parser() = scriptParser {
            ConversationOrigin()
        }
    }
}