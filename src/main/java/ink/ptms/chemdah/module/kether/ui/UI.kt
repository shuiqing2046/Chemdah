package ink.ptms.chemdah.module.kether.ui

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.core.quest.meta.MetaType.Companion.type
import ink.ptms.chemdah.util.UI
import ink.ptms.chemdah.util.colored
import ink.ptms.chemdah.util.getProfile
import io.izzel.taboolib.kotlin.kether.Kether.expects
import io.izzel.taboolib.kotlin.kether.KetherParser
import io.izzel.taboolib.kotlin.kether.ScriptParser
import io.izzel.taboolib.kotlin.kether.common.api.QuestAction
import io.izzel.taboolib.kotlin.kether.common.api.QuestContext
import io.izzel.taboolib.kotlin.kether.common.loader.types.ArgTypes
import io.izzel.taboolib.util.Coerce
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.ui.UI
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class UI {

    class UIBar(val plan: String, val include: List<String>, val exclude: List<String>) : QuestAction<String>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<String> {
            val profile = frame.getProfile()
            val quests = ChemdahAPI.questTemplate.filter { (_, v) -> v.type().any { it in include } && v.type().none { it in exclude } }.values.toList()
            val percent = quests.count { profile.isQuestCompleted(it) } / quests.size.toDouble()
            val ui = frame.UI()
            val empty = ui.config.getString("bar.$plan.empty", "&8|")!!.colored()
            val fill = ui.config.getString("bar.$plan.fill", "&a|")!!.colored()
            val size = ui.config.getInt("bar.$plan.size", 35)
            return CompletableFuture.completedFuture((1..size).joinToString("") {
                if (percent.isNaN() || percent == 0.0) empty else if (percent >= it.toDouble() / size) fill else empty
            })
        }

        override fun toString(): String {
            return "UIBar(plan='$plan', include=$include, exclude=$exclude)"
        }

    }

    class UIPercent(val include: List<String>, val exclude: List<String>) : QuestAction<String>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<String> {
            val profile = frame.getProfile()
            val quests = ChemdahAPI.questTemplate.filter { (_, v) -> v.type().any { it in include } && v.type().none { it in exclude } }.values.toList()
            val percent = quests.count { profile.isQuestCompleted(it) } / quests.size.toDouble()
            return if (percent.isNaN()) {
                CompletableFuture.completedFuture("0")
            } else {
                CompletableFuture.completedFuture(Coerce.format(percent * 100).toString())
            }
        }

        override fun toString(): String {
            return "UIPercent(include=$include, exclude=$exclude)"
        }

    }

    companion object {

        private val tokenType = ArgTypes.listOf { reader ->
            reader.nextToken()
        }

        @KetherParser(["ui"], namespace = "chemdah-quest-ui")
        fun parser() = ScriptParser.parser {
            when (it.expects("bar", "percent")) {
                "bar" -> UIBar(it.nextToken(), it.next(tokenType), it.run {
                    try {
                        it.mark()
                        it.expect("exclude")
                        it.next(tokenType)
                    } catch (ex: Throwable) {
                        it.reset()
                        emptyList()
                    }
                })
                "percent" -> UIPercent(it.next(tokenType), it.run {
                    try {
                        it.mark()
                        it.expect("exclude")
                        it.next(tokenType)
                    } catch (ex: Throwable) {
                        it.reset()
                        emptyList()
                    }
                })
                else -> error("out of case")
            }
        }
    }
}