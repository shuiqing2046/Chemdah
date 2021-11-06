package ink.ptms.chemdah.module.kether.quest

import ink.ptms.chemdah.core.quest.Task
import ink.ptms.chemdah.util.getQuestContainer
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.scriptParser
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.quest.QuestThis
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class QuestThis(val task: Boolean) : ScriptAction<String>() {

    override fun run(frame: ScriptFrame): CompletableFuture<String> {
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

        @KetherParser(["this"], namespace = "chemdah-quest", shared = true)
        fun parser() = scriptParser {
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