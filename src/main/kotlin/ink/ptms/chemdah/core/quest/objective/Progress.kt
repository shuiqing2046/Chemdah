package ink.ptms.chemdah.core.quest.objective

import taboolib.common5.Coerce
import taboolib.common5.cdouble

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.Progress
 *
 * @author sky
 * @since 2021/3/3 4:51 下午
 */
open class Progress(val value: Any, val target: Any, percent: Double) {

    val percent = Coerce.format(percent.coerceAtMost(1.0).coerceAtLeast(0.0))

    companion object {

        @JvmStatic
        val ZERO = Progress(0, 0, 0.0)

        fun Any.toProgress(target: Any, percent: Double) = Progress(this, target, percent)

        fun Number.toProgress(target: Number) = Progress(this, target, cdouble / target.cdouble)
    }
}