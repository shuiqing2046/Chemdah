package ink.ptms.chemdah.core.quest.objective

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.Proces
 *
 * @author sky
 * @since 2021/3/3 4:51 下午
 */
open class Progress(val value: Any, val target: Any, val percent: Double) {

    companion object {

        fun Any.progress(target: Any, percent: Double) = Progress(this, target, percent)
    }
}