package ink.ptms.chemdah.module.kether.quest

import ink.ptms.chemdah.core.quest.Task
import ink.ptms.chemdah.core.quest.Template
import ink.ptms.chemdah.util.getQuest
import ink.ptms.chemdah.util.getQuestContainer
import io.izzel.taboolib.kotlin.kether.KetherParser
import io.izzel.taboolib.kotlin.kether.ScriptParser
import io.izzel.taboolib.kotlin.kether.common.api.QuestAction
import io.izzel.taboolib.kotlin.kether.common.api.QuestContext
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.quest.QuestThis
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class QuestThis(val task: Boolean) : QuestAction<String>() {

    override fun process(frame: QuestContext.Frame): CompletableFuture<String> {
        val container = frame.getQuestContainer()
        val id = if (container is Task) {
            if (task) {
                container.id
            } else {
                container.template.id
            }
        } else {
            container.id
        }
        return CompletableFuture.completedFuture(id)
    }

    companion object {

        @KetherParser(["this"], namespace = "chemdah-quest")
        fun parser() = ScriptParser.parser {
            QuestThis(try {
                it.mark()
                it.expect("task")
                true
            } catch (ex: Exception) {
                it.reset()
                false
            })
        }
    }
}