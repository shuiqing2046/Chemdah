package ink.ptms.chemdah.module.kether.compat

import ch.njol.skript.variables.Variables
import io.izzel.taboolib.kotlin.kether.Kether.expects
import io.izzel.taboolib.kotlin.kether.KetherParser
import io.izzel.taboolib.kotlin.kether.ScriptParser
import io.izzel.taboolib.kotlin.kether.common.api.ParsedAction
import io.izzel.taboolib.kotlin.kether.common.api.QuestAction
import io.izzel.taboolib.kotlin.kether.common.api.QuestContext
import io.izzel.taboolib.kotlin.kether.common.loader.types.ArgTypes
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.ActionSkript
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class ActionSkript {

    class SkriptVar(val key: ParsedAction<*>) : QuestAction<Any>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<Any> {
            return frame.newFrame(key).run<Any>().thenApply {
                Variables.getVariable(it.toString(), null, false)
            }
        }

        override fun toString(): String {
            return "SkriptVar(key=$key)"
        }

    }

    companion object {

        /**
         * sk var *name
         * sk exec [ set player's level to 10 ]
         */
        @KetherParser(["skript", "sk"])
        fun parser() = ScriptParser.parser {
            when (it.expects("var")) {
                "var" -> SkriptVar(it.next(ArgTypes.ACTION))
                else -> error("out of case")
            }
        }
    }
}