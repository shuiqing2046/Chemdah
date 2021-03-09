package ink.ptms.chemdah.core.quest.meta

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Id
import ink.ptms.chemdah.core.quest.QuestContainer
import ink.ptms.chemdah.util.asList
import ink.ptms.chemdah.util.mirrorFuture
import ink.ptms.chemdah.util.namespaceQuest
import io.izzel.taboolib.kotlin.kether.KetherShell
import io.izzel.taboolib.util.Coerce
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.meta.MetaRestart
 *
 * @author sky
 * @since 2021/3/1 11:47 下午
 */
@Id("restart")
@MetaType(MetaType.Type.ANY)
class MetaRestart(source: Any?, questContainer: QuestContainer) : Meta<Any?>(source, questContainer) {

    val restart = source?.asList() ?: emptyList()

    companion object {

        /**
         * 检查任务是否满足重置条件
         */
        fun QuestContainer.restart(profile: PlayerProfile): CompletableFuture<Boolean> {
            val future = CompletableFuture<Boolean>()
            mirrorFuture("AddonRestart:checkRestart") {
                val reset = meta<MetaRestart>("restart")?.restart
                if (reset == null || reset.isEmpty()) {
                    future.complete(false)
                    finish()
                } else {
                    KetherShell.eval(reset, namespace = namespaceQuest) {
                        sender = profile.player
                        rootFrame().variables().also { vars ->
                            vars.set("@Quest", getQuest(profile))
                            vars.set("@QuestContainer", this@restart)
                        }
                    }.thenApply {
                        future.complete(Coerce.toBoolean(it))
                        finish()
                    }
                }
            }
            return future
        }
    }
}