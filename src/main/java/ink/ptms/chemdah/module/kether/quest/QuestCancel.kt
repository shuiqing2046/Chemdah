package ink.ptms.chemdah.module.kether.quest

import io.izzel.taboolib.kotlin.kether.KetherParser
import io.izzel.taboolib.kotlin.kether.ScriptParser
import io.izzel.taboolib.kotlin.kether.common.api.QuestAction
import io.izzel.taboolib.kotlin.kether.common.api.QuestContext
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.quest.QuestCancel
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class QuestCancel : QuestAction<Boolean>() {

    override fun process(frame: QuestContext.Frame): CompletableFuture<Boolean> {
        return CompletableFuture.completedFuture(false)
    }

    companion object {

        @KetherParser(["cancel"], namespace = "chemdah-quest")
        fun parser() = ScriptParser.parser {
            QuestCancel()
        }
    }
}