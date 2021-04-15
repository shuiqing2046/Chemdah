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
abstract class Theme<T : ThemeSettings> {

    lateinit var settings: T

    open fun reloadConfig() {
    }

    open fun sendEffect(): Boolean {
        return true
    }

    open fun allowFarewell(): Boolean {
        return true
    }

    open fun reload(session: Session): CompletableFuture<Void> {
        return CompletableFuture.completedFuture(null)
    }

    open fun begin(session: Session): CompletableFuture<Void> {
        return npcTalk(session, session.npcSide)
    }

    open fun end(session: Session): CompletableFuture<Void> {
        return CompletableFuture.completedFuture(null)
    }

    abstract fun npcTalk(session: Session, message: List<String>, canReply: Boolean = true): CompletableFuture<Void>
}