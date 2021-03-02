package ink.ptms.chemdah.core.conversation.theme

import ink.ptms.chemdah.core.conversation.Session
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.core.conversation.theme.Theme
 *
 * @author sky
 * @since 2021/2/10 10:27 上午
 */
interface Theme {

    fun reloadConfig() {
    }

    fun reload(session: Session): CompletableFuture<Void>

    fun begin(session: Session): CompletableFuture<Void>

    fun end(session: Session): CompletableFuture<Void>

    fun npcTalk(session: Session, message: List<String>, canReply: Boolean = true): CompletableFuture<Void>
}