package ink.ptms.chemdah.module.kether

import ink.ptms.chemdah.core.quest.selector.InferArea
import ink.ptms.chemdah.core.quest.selector.InferArea.Companion.toInferArea
import ink.ptms.chemdah.util.getPlayer
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.ConditionPosition
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class ActionPosition(val area: InferArea) : ScriptAction<Boolean>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Boolean> {
        return CompletableFuture.completedFuture(area.inside(frame.getPlayer().location))
    }

    companion object {

        /**
         * position inside "world 0 0 0 > 10 10 10"
         */
        @KetherParser(["position"], shared = true)
        fun parser() = scriptParser {
            it.expects("is", "in", "inside")
            ActionPosition(it.nextToken().toInferArea())
        }
    }
}