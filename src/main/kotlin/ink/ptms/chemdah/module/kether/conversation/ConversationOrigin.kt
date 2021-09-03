package ink.ptms.chemdah.module.kether.conversation

import ink.ptms.chemdah.util.getSession
import taboolib.module.kether.*
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import org.bukkit.Location
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