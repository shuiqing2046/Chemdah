package ink.ptms.chemdah.core.quest.addon

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Id
import ink.ptms.chemdah.core.quest.Option
import ink.ptms.chemdah.core.quest.QuestContainer
import ink.ptms.chemdah.util.namespaceQuest
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.platform.function.warning
import taboolib.common.util.asList
import taboolib.common5.Coerce
import taboolib.module.kether.KetherShell
import taboolib.module.kether.printKetherErrorMessage
import taboolib.module.kether.runKether
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.addon.AddonRestart
 *
 * @author sky
 * @since 2021/3/1 11:47 下午
 */
@Id("restart")
@Option(Option.Type.ANY)
class AddonRestart(root: Any?, questContainer: QuestContainer) : Addon(root, questContainer) {

    val restart = root?.asList() ?: emptyList()

    companion object {

        /**
         * 检查任务是否满足重置条件
         */
        fun QuestContainer.canRestart(profile: PlayerProfile): CompletableFuture<Boolean> {
            return CompletableFuture<Boolean>().also { future ->
                val reset = addon<AddonRestart>("restart")?.restart
                if (reset.isNullOrEmpty()) {
                    future.complete(false)
                } else {
                    try {
                        KetherShell.eval(reset, sender = adaptPlayer(profile.player), namespace = namespaceQuest) {
                            rootFrame().variables().also { vars ->
                                vars.set("@QuestContainer", this@canRestart)
                            }
                        }.thenApply {
                            future.complete(Coerce.toBoolean(it))
                        }
                    } catch (e: Throwable) {
                        warning("path: $path, addon: restart, source: $reset")
                        e.printKetherErrorMessage()
                        future.complete(false)
                    }
                }
            }
        }
    }
}