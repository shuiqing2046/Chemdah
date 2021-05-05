package ink.ptms.chemdah.core.conversation.theme

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.core.conversation.PlayerReply
import ink.ptms.chemdah.core.conversation.Session
import java.lang.Exception
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

    fun register(name: String) {
        ChemdahAPI.conversationTheme[name] = this
    }

    abstract fun createConfig(): T

    open fun reloadConfig() {
        settings = createConfig()
    }

    open fun allowFarewell(): Boolean {
        return true
    }

    open fun onReset(session: Session): CompletableFuture<Void> {
        return CompletableFuture.completedFuture(null)
    }

    open fun onBegin(session: Session): CompletableFuture<Void> {
        settings.playSound(session)
        return onDisplay(session, session.npcSide)
    }

    open fun onClose(session: Session): CompletableFuture<Void> {
        return CompletableFuture.completedFuture(null)
    }

    abstract fun onDisplay(session: Session, message: List<String>, canReply: Boolean = true): CompletableFuture<Void>

    protected fun Session.createTitle(): String {
        return conversation.option.title.replace("{name}", npcName)
    }

    protected fun Session.createDisplay(func: (List<PlayerReply>) -> (Unit)): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        npcTalking = true
        conversation.playerSide.checked(this).thenAccept { replies ->
            try {
                func(replies)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            future.complete(null)
        }
        future.thenAccept {
            npcTalking = false
        }
        return future
    }
}