package ink.ptms.chemdah.module.kether.conversation

import ink.ptms.chemdah.util.getSession
import io.izzel.taboolib.kotlin.kether.KetherParser
import io.izzel.taboolib.kotlin.kether.ScriptParser
import io.izzel.taboolib.kotlin.kether.common.api.QuestAction
import io.izzel.taboolib.kotlin.kether.common.api.QuestContext
import org.bukkit.Location
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.ConversationLocation
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class ConversationOrigin : QuestAction<Location>() {

    override fun process(frame: QuestContext.Frame): CompletableFuture<Location> {
        return CompletableFuture.completedFuture(frame.getSession().origin)
    }

    override fun toString(): String {
        return "ConversationOrigin()"
    }

    companion object {

        @KetherParser(["origin"], namespace = "chemdah-conversation")
        fun parser() = ScriptParser.parser {
            ConversationOrigin()
        }
    }
}