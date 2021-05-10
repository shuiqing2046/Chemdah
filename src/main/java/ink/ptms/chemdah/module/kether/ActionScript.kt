package ink.ptms.chemdah.module.kether

import com.google.common.collect.ImmutableList
import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.util.getPlayer
import ink.ptms.chemdah.util.print
import io.izzel.taboolib.kotlin.kether.Kether.expects
import io.izzel.taboolib.kotlin.kether.KetherParser
import io.izzel.taboolib.kotlin.kether.ScriptContext
import io.izzel.taboolib.kotlin.kether.ScriptParser
import io.izzel.taboolib.kotlin.kether.common.api.ParsedAction
import io.izzel.taboolib.kotlin.kether.common.api.QuestAction
import io.izzel.taboolib.kotlin.kether.common.api.QuestContext
import io.izzel.taboolib.kotlin.kether.common.loader.types.ArgTypes
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.ActionScript
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class ActionScript {

    class ScriptRun(val name: String, val self: Boolean, val using: List<ParsedAction<*>>) : QuestAction<Void>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<Void> {
            val future = CompletableFuture<Void>()
            val args = ArrayList<Any>()
            fun process(cur: Int) {
                if (cur < using.size) {
                    frame.newFrame(using[cur]).run<Any>().thenAccept {
                        args.add(it)
                        process(cur + 1)
                    }
                } else {
                    val script = ChemdahAPI.workspace.scripts[name]
                    if (script != null) {
                        try {
                            ChemdahAPI.workspace.runScript(if (self) "$name@${frame.getPlayer().name}" else name, ScriptContext.create(script) {
                                sender = frame.getPlayer()
                                args.forEachIndexed { index, any ->
                                    rootFrame().variables().set("arg$index", any)
                                }
                            })
                        } catch (t: Throwable) {
                            t.print()
                        }
                    }
                    future.complete(null)
                }
            }
            process(0)
            return future
        }

        override fun toString(): String {
            return "ScriptRun(name='$name', self=$self)"
        }
    }

    class ScriptStop(val name: String, val self: Boolean) : QuestAction<Void>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<Void> {
            val namespace = if (self) "$name@${frame.getPlayer().name}" else name
            val script = ImmutableList.copyOf(ChemdahAPI.workspace.getRunningScript()).firstOrNull { it.quest.id == namespace }
            if (script != null) {
                ChemdahAPI.workspace.terminateScript(script)
            }
            return CompletableFuture.completedFuture(null)
        }

        override fun toString(): String {
            return "ScriptStop(name='$name', self=$self)"
        }
    }

    companion object {

        /**
         * script run def using [ *arg0 *arg1 ]
         * script stop def
         */
        @KetherParser(["script"], namespace = "chemdah")
        fun parser() = ScriptParser.parser {
            val action = it.expects("run", "stop", "terminate")
            val name = it.nextToken()
            val self = try {
                it.mark()
                it.expect("@self")
                true
            } catch (ex: Exception) {
                it.reset()
                false
            }
            when (action) {
                "run" -> {
                    ScriptRun(
                        name, self, try {
                            it.mark()
                            it.expect("using")
                            it.next(ArgTypes.listOf(ArgTypes.ACTION))
                        } catch (ex: Exception) {
                            it.reset()
                            emptyList()
                        }
                    )
                }
                "stop", "terminate" -> ScriptStop(name, self)
                else -> error("out of case")
            }
        }
    }
}