package ink.ptms.chemdah.module.kether

import ink.ptms.chemdah.api.ChemdahAPI
import taboolib.common.platform.function.submitAsync
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.ActionVariables
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class ActionVariables {

    class VariablesGet(val key: ParsedAction<*>, val default: ParsedAction<*> = ParsedAction.noop<Any>()) : ScriptAction<Any?>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
            val future = CompletableFuture<Any?>()
            frame.run(key).str { key -> frame.run(default).str { def -> future.complete(ChemdahAPI.getVariable(key) ?: def) } }
            return future
        }
    }

    class VariablesSet(
        val key: ParsedAction<*>,
        val value: ParsedAction<*>,
        val symbol: PlayerOperator.Method,
        val default: ParsedAction<*> = ParsedAction.noop<Any>(),
    ) : ScriptAction<Void>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Void> {
            frame.run(key).str { key ->
                frame.run(value).str { value ->
                    frame.run(default).str { def ->
                        submitAsync { ChemdahAPI.setVariable(key, value, symbol == PlayerOperator.Method.INCREASE, def) }
                    }
                }
            }
            return CompletableFuture.completedFuture(null)
        }
    }

    class VariablesKeys : ScriptAction<List<String>>() {

        override fun run(frame: ScriptFrame): CompletableFuture<List<String>> {
            return CompletableFuture.completedFuture(ChemdahAPI.getVariables())
        }
    }

    companion object {

        @KetherParser(["var"], namespace = "chemdah", shared = true)
        fun parser() = scriptParser {
            try {
                it.mark()
                it.expect("keys")
                VariablesKeys()
            } catch (ex: Throwable) {
                it.reset()
                val key = it.next(ArgTypes.ACTION)
                try {
                    it.mark()
                    when (it.expects("=", "+", "to", "add", "increase")) {
                        "=", "to" -> VariablesSet(key, it.next(ArgTypes.ACTION), PlayerOperator.Method.MODIFY)
                        "+", "add", "increase" -> {
                            val value = it.next(ArgTypes.ACTION)
                            try {
                                it.mark()
                                it.expect("default")
                                VariablesSet(key, value, PlayerOperator.Method.INCREASE, it.next(ArgTypes.ACTION))
                            } catch (ex: Throwable) {
                                it.reset()
                                VariablesSet(key, value, PlayerOperator.Method.INCREASE)
                            }
                        }

                        else -> error("out of case")
                    }
                } catch (ex: Throwable) {
                    it.reset()
                    try {
                        it.mark()
                        it.expect("default")
                        VariablesGet(key, it.next(ArgTypes.ACTION))
                    } catch (ex: Throwable) {
                        it.reset()
                        VariablesGet(key)
                    }
                }
            }
        }
    }
}