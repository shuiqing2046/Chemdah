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

    class VariablesGet(val key: ParsedAction<*>) : QuestAction<String?>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<String?> {
            return frame.newFrame(key).run<Any>().thenApply {
                ChemdahAPI.getVariable(it.toString())
            }
        }
    }

    class VariablesSet(val key: ParsedAction<*>, val value: ParsedAction<*>, val symbol: Symbol) : QuestAction<Void>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<Void> {
            return frame.newFrame(key).run<Any>().thenAccept { key ->
                frame.newFrame(value).run<Any?>().thenAccept { value ->
                    Tasks.task(true) {
                        ChemdahAPI.setVariable(key.toString(), value?.toString(), symbol == Symbol.ADD)
                    }
                }
            }
        }
    }

    class VariablesKeys : QuestAction<List<String>>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<List<String>> {
            return CompletableFuture.completedFuture(ChemdahAPI.getVariables())
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
                        "add", "increase" -> VariablesSet(key, it.next(ArgTypes.ACTION), Symbol.ADD)
                        else -> error("out of case")
                    }
                } catch (ex: Throwable) {
                    it.reset()
                    VariablesGet(key)
                }
            }
        }
    }
}