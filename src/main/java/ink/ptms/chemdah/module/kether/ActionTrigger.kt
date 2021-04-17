package ink.ptms.chemdah.module.kether

import ink.ptms.chemdah.api.ChemdahAPI.callTrigger
import ink.ptms.chemdah.util.getPlayer
import io.izzel.taboolib.kotlin.kether.KetherParser
import io.izzel.taboolib.kotlin.kether.ScriptParser
import io.izzel.taboolib.kotlin.kether.common.api.QuestAction
import io.izzel.taboolib.kotlin.kether.common.api.QuestContext
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
        frame.getPlayer().callTrigger(trigger)
        return CompletableFuture.completedFuture(null)
    }

    companion object {

        /**
         * trigger def
         */
        @KetherParser(["trigger"], namespace = "chemdah")
        fun parser() = ScriptParser.parser {
            ActionTrigger(it.nextToken())
        }
    }
}