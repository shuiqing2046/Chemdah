package ink.ptms.chemdah.module.kether

import com.google.common.collect.ImmutableList
import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.api.ChemdahAPI.callTrigger
import ink.ptms.chemdah.util.getPlayer
import ink.ptms.chemdah.util.print
import io.izzel.taboolib.kotlin.kether.Kether.expects
import io.izzel.taboolib.kotlin.kether.KetherParser
import io.izzel.taboolib.kotlin.kether.ScriptContext
import io.izzel.taboolib.kotlin.kether.ScriptParser
import io.izzel.taboolib.kotlin.kether.common.api.QuestAction
import io.izzel.taboolib.kotlin.kether.common.api.QuestContext
import io.izzel.taboolib.kotlin.sendLocale
import org.bukkit.Bukkit
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.ActionScript
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class ActionScript {

    class ScriptRun(val name: String, val self: Boolean) : QuestAction<Void>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<Void> {
            val script = ChemdahAPI.workspace.scripts[name]
            if (script != null) {
                try {
                    ChemdahAPI.workspace.runScript(if (self) "$name@${frame.getPlayer().name}" else name, ScriptContext.create(script) {
                        sender = frame.getPlayer()
                    })
                } catch (t: Throwable) {
                    t.print()
                }
            }
            return CompletableFuture.completedFuture(null)
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
    }

    companion object {

        /**
         * script run def
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
                "run" -> ScriptRun(name, self)
                "stop", "terminate" -> ScriptStop(name, self)
                else -> error("out of case")
            }
        }
    }
}