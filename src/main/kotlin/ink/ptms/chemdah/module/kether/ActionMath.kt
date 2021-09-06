package ink.ptms.chemdah.module.kether

import taboolib.common5.Coerce
import taboolib.library.kether.ArgTypes
import taboolib.module.kether.KetherParser
import taboolib.module.kether.actionFuture
import taboolib.module.kether.scriptParser

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.ActionMath
 *
 * @author sky
 * @since 2021/6/14 2:59 下午
 */
class ActionMath {

    companion object {

        @KetherParser(["max"], namespace = "chemdah", shared = true)
        fun max() = scriptParser {
            val n1 = it.next(ArgTypes.ACTION)
            val n2 = it.next(ArgTypes.ACTION)
            actionFuture {
                newFrame(n1).run<Any>().thenAccept { n1 ->
                    newFrame(n2).run<Any>().thenAccept { n2 ->
                        it.complete(kotlin.math.max(Coerce.toDouble(n1), Coerce.toDouble(n2)))
                    }
                }
            }
        }

        @KetherParser(["ceil"], namespace = "chemdah", shared = true)
        fun ceil() = scriptParser {
            val n1 = it.next(ArgTypes.ACTION)
            actionFuture {
                newFrame(n1).run<Any>().thenAccept { n1 ->
                    it.complete(kotlin.math.ceil(Coerce.toDouble(n1)))
                }
            }
        }
    }
}