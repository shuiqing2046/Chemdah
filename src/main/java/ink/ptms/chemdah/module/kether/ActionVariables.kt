package ink.ptms.chemdah.module.kether

import ink.ptms.chemdah.api.ChemdahAPI
import io.izzel.taboolib.kotlin.Tasks
import io.izzel.taboolib.kotlin.kether.Kether.expects
import io.izzel.taboolib.kotlin.kether.KetherParser
import io.izzel.taboolib.kotlin.kether.ScriptParser
import io.izzel.taboolib.kotlin.kether.action.bukkit.Symbol
import io.izzel.taboolib.kotlin.kether.common.api.ParsedAction
import io.izzel.taboolib.kotlin.kether.common.api.QuestAction
import io.izzel.taboolib.kotlin.kether.common.api.QuestContext
import io.izzel.taboolib.kotlin.kether.common.loader.types.ArgTypes
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.ActionVariables
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class ActionVariables {

    class VariablesGet(val key: ParsedAction<*>, val default: ParsedAction<*> = ParsedAction.noop<Any>()) : QuestAction<Any>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<Any> {
            return frame.newFrame(key).run<Any>().thenApply {
                frame.newFrame(default).run<Any>().thenApply { def ->
                    ChemdahAPI.getVariable(it.toString()) ?: def
                }
            }
        }

        override fun toString(): String {
            return "VariablesGet(key=$key, default=$default)"
        }
    }

    class VariablesSet(
        val key: ParsedAction<*>,
        val value: ParsedAction<*>,
        val symbol: Symbol,
        val default: ParsedAction<*> = ParsedAction.noop<Any>()
    ) : QuestAction<Void>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<Void> {
            return frame.newFrame(key).run<Any>().thenAccept { key ->
                frame.newFrame(value).run<Any>().thenAccept { value ->
                    frame.newFrame(default).run<Any>().thenAccept { def ->
                        Tasks.task(true) {
                            ChemdahAPI.setVariable(key.toString(), value?.toString(), symbol == Symbol.ADD, default = def?.toString())
                        }
                    }
                }
            }
        }

        override fun toString(): String {
            return "VariablesSet(key=$key, value=$value, symbol=$symbol, default=$default)"
        }
    }

    class VariablesKeys : QuestAction<List<String>>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<List<String>> {
            return CompletableFuture.completedFuture(ChemdahAPI.getVariables())
        }

        override fun toString(): String {
            return "VariablesKeys()"
        }
    }

    companion object {

        @KetherParser(["var"], namespace = "chemdah")
        fun parser() = ScriptParser.parser {
            try {
                it.mark()
                it.expect("keys")
                VariablesKeys()
            } catch (ex: Throwable) {
                it.reset()
                val key = it.next(ArgTypes.ACTION)
                try {
                    it.mark()
                    when (it.expects("to", "add", "increase")) {
                        "to" -> VariablesSet(key, it.next(ArgTypes.ACTION), Symbol.SET)
                        "add", "increase" -> {
                            val value = it.next(ArgTypes.ACTION)
                            try {
                                it.mark()
                                it.expect("default")
                                VariablesSet(key, value, Symbol.ADD, it.next(ArgTypes.ACTION))
                            } catch (ex: Throwable) {
                                it.reset()
                                VariablesSet(key, value, Symbol.ADD)
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