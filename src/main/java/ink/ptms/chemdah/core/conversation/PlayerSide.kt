package ink.ptms.chemdah.core.conversation

import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.core.conversation.PlayerSIde
 *
 * @author sky
 * @since 2021/2/9 6:23 下午
 */
data class PlayerSide(
    val reply: List<PlayerReply>
) {

    fun checked(session: Session): CompletableFuture<List<PlayerReply>> {
        val future = CompletableFuture<List<PlayerReply>>()
        val r = ArrayList<PlayerReply>()
        fun process(cur: Int) {
            if (cur < reply.size) {
                val reply = reply[cur]
                reply.check(session).thenApply {
                    if (it) {
                        r.add(reply)
                    }
                    process(cur + 1)
                }
            } else {
                future.complete(r)
            }
        }
        process(0)
        return future
    }
}