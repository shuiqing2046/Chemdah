package ink.ptms.chemdah.module.kether.compat

import ch.njol.skript.variables.Variables
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.ActionSkript
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class ActionSkript {

    class SkriptVar(val key: ParsedAction<*>) : ScriptAction<Any>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Any> {
            return frame.newFrame(key).run<Any>().thenApply {
                Variables.getVariable(it.toString(), null, false)
            }
        }
    }

    companion object {

        /**
         * sk var *name
         * sk exec [ set player's level to 10 ]
         */
        @KetherParser(["skript", "sk"], shared = true)
        fun parser() = scriptParser {
            when (it.expects("var")) {
                "var" -> SkriptVar(it.next(ArgTypes.ACTION))
                else -> error("out of case")
            }
        }
    }
}