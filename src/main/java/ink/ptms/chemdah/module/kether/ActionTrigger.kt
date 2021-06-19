package ink.ptms.chemdah.module.kether

import ink.ptms.chemdah.api.ChemdahAPI.callTrigger
import io.izzel.taboolib.kotlin.kether.KetherParser
import io.izzel.taboolib.kotlin.kether.ScriptParser
import io.izzel.taboolib.kotlin.kether.common.api.QuestAction
import io.izzel.taboolib.kotlin.kether.common.api.QuestContext
import io.izzel.taboolib.kotlin.kether.script
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.ActionTrigger
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class ActionTrigger(val trigger: String) : QuestAction<Void>() {

    override fun process(frame: QuestContext.Frame): CompletableFuture<Void> {
        val player = frame.script().sender as? Player ?: error("No player selected.")
        player.callTrigger(trigger)
        return CompletableFuture.completedFuture(null)
    }

    override fun toString(): String {
        return "ActionTrigger(trigger='$trigger')"
    }

    companion object {

        /**
         * trigger def
         */
        @KetherParser(["trigger"])
        fun parser() = ScriptParser.parser {
            ActionTrigger(it.nextToken())
        }
    }
}