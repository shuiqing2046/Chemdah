package ink.ptms.chemdah.module.kether

import ink.ptms.chemdah.util.getProfile
import ink.ptms.chemdah.util.increaseAny
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
 * ink.ptms.chemdah.module.kether.ActionProfile
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class ActionProfile {

    class ProfileDataGet(val key: ParsedAction<*>) : QuestAction<Any?>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<Any?> {
            return frame.newFrame(key).run<Any>().thenApply {
                frame.getProfile().persistentDataContainer[it.toString()]?.value
            }
        }
    }

    class ProfileDataSet(val key: ParsedAction<*>, val value: ParsedAction<*>, val symbol: Symbol) : QuestAction<Void>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<Void> {
            return frame.newFrame(key).run<Any>().thenAccept { key ->
                frame.newFrame(value).run<Any?>().thenAccept { value ->
                    val persistentDataContainer = frame.getProfile().persistentDataContainer
                    when {
                        value == null -> {
                            persistentDataContainer.remove(key.toString())
                        }
                        symbol == Symbol.ADD -> {
                            persistentDataContainer[key.toString()] = persistentDataContainer[key.toString()].increaseAny(value)
                        }
                        else -> {
                            persistentDataContainer[key.toString()] = value
                        }
                    }
                }
            }
        }
    }

    class ProfileDataKeys : QuestAction<List<String>>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<List<String>> {
            return CompletableFuture.completedFuture(frame.getProfile().persistentDataContainer.keys())
        }
    }

    companion object {

        /**
         * profile data *key
         * profile data *key to *value
         * profile data *key add *value
         * profile data keys
         * profile data changed
         */
        @KetherParser(["profile"], namespace = "chemdah")
        fun parser() = ScriptParser.parser {
            when (it.expects("data")) {
                "data" -> {
                    try {
                        it.mark()
                        it.expect("keys")
                        ProfileDataKeys()
                    } catch (ex: Throwable) {
                        it.reset()
                        val key = it.next(ArgTypes.ACTION)
                        try {
                            it.mark()
                            when (it.expects("to", "add", "increase")) {
                                "to" -> ProfileDataSet(key, it.next(ArgTypes.ACTION), Symbol.SET)
                                "add", "increase" -> ProfileDataSet(key, it.next(ArgTypes.ACTION), Symbol.ADD)
                                else -> error("out of case")
                            }
                        } catch (ex: Throwable) {
                            it.reset()
                            ProfileDataGet(key)
                        }
                    }
                }
                else -> error("out of case")
            }
        }
    }
}