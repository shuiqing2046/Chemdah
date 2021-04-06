package ink.ptms.chemdah.module.kether

import ink.ptms.chemdah.util.getPlayer
import ink.ptms.chemdah.util.selector.InferArea
import ink.ptms.chemdah.util.selector.InferArea.Companion.toInferArea
import io.izzel.taboolib.kotlin.kether.Kether.expects
import io.izzel.taboolib.kotlin.kether.KetherParser
import io.izzel.taboolib.kotlin.kether.ScriptParser
import io.izzel.taboolib.kotlin.kether.common.api.QuestAction
import io.izzel.taboolib.kotlin.kether.common.api.QuestContext
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.ConditionPosition
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class ConditionPosition(val area: InferArea) : QuestAction<Boolean>() {

    override fun process(frame: QuestContext.Frame): CompletableFuture<Boolean> {
        return CompletableFuture.completedFuture(area.inside(frame.getPlayer().location))
    }

    companion object {

        /**
         * position inside "world 0 0 0 ~ 10 10 10"
         */
        @KetherParser(["position"], namespace = "chemdah")
        fun parser() = ScriptParser.parser {
            it.expects("is", "in", "inside")
            ConditionPosition(it.nextToken().toInferArea())
        }
    }
}