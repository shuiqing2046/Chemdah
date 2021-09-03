package ink.ptms.chemdah.module.kether

import ink.ptms.chemdah.api.ChemdahAPI.callTrigger
import org.bukkit.entity.Player
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.ActionTrigger
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class ActionTrigger(val trigger: String) : ScriptAction<Void>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Void> {
        val player = frame.script().sender as? Player ?: error("No player selected.")
        player.callTrigger(trigger)
        return CompletableFuture.completedFuture(null)
    }

    companion object {

        /**
         * trigger def
         */
        @KetherParser(["trigger"], shared = true)
        fun parser() = scriptParser {
            ActionTrigger(it.nextToken())
        }
    }
}