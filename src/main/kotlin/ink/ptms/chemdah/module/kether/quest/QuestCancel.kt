package ink.ptms.chemdah.module.kether.quest

import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.scriptParser
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.quest.QuestCancel
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class QuestCancel : ScriptAction<Boolean>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Boolean> {
        return CompletableFuture.completedFuture(false)
    }

    companion object {

        @KetherParser(["cancel"], namespace = "chemdah-quest")
        fun parser() = scriptParser {
            QuestCancel()
        }
    }
}