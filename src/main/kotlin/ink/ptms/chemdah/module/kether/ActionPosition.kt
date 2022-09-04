package ink.ptms.chemdah.module.kether

import ink.ptms.chemdah.core.quest.selector.InferArea.Companion.toInferArea
import ink.ptms.chemdah.util.getBukkitPlayer
import taboolib.module.kether.KetherParser
import taboolib.module.kether.actionNow
import taboolib.module.kether.expects
import taboolib.module.kether.scriptParser

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.ConditionPosition
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
object ActionPosition {

    /**
     * position inside "world 0 0 0 > 10 10 10"
     */
    @KetherParser(["position"], shared = true)
    fun parser() = scriptParser {
        it.expects("is", "in", "inside")
        val area = it.nextToken().toInferArea()
        actionNow { area.inside(getBukkitPlayer().location) }
    }
}