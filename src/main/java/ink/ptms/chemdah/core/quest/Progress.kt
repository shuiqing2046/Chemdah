package ink.ptms.chemdah.core.quest

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.Proces
 *
 * @author sky
 * @since 2021/3/3 4:51 下午
 */
abstract class Progress(val current: Any, val target: Any) {

    abstract val percent: Double

    companion object {

        fun Any.to(target: Any, percent: Double) = let {
            object : Progress(it, target) {
                override val percent = percent
            }
        }
    }
}