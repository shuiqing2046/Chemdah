package ink.ptms.chemdah.module.kether

import com.google.common.collect.ImmutableList
import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.util.getPlayer
import taboolib.common.platform.function.adaptPlayer
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.ActionScript
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class ActionScript {

    class ScriptRun(val name: String, val self: Boolean, val using: List<ParsedAction<*>>) : ScriptAction<Void>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Void> {
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
                                sender = adaptPlayer(frame.getPlayer())
                                args.forEachIndexed { index, any ->
                                    rootFrame().variables().set("arg$index", any)
                                }
                            })
                        } catch (t: Throwable) {
                            t.printKetherErrorMessage()
                        }
                    }
                    future.complete(null)
                }
            }
            process(0)
            return future
        }
    }

    class ScriptStop(val name: String, val self: Boolean) : ScriptAction<Void>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Void> {
            val namespace = if (self) "$name@${frame.getPlayer().name}" else name
            val script = ImmutableList.copyOf(ChemdahAPI.workspace.getRunningScript()).firstOrNull { it.quest.id == namespace }
            if (script != null) {
                ChemdahAPI.workspace.terminateScript(script)
            }
            return CompletableFuture.completedFuture(null)
        }
    }

    companion object {

        /**
         * script run def using [ *arg0 *arg1 ]
         * script stop def
         */
        @KetherParser(["script"], namespace = "chemdah", shared = true)
        fun parser() = scriptParser {
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