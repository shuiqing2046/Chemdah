package ink.ptms.chemdah.core.quest.addon

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Id
import ink.ptms.chemdah.core.quest.QuestContainer
import ink.ptms.chemdah.core.quest.Task
import ink.ptms.chemdah.core.quest.objective.Progress
import ink.ptms.chemdah.util.asList
import ink.ptms.chemdah.util.namespaceQuest
import ink.ptms.chemdah.util.warning
import io.izzel.taboolib.kotlin.kether.KetherShell
import io.izzel.taboolib.util.Coerce
import org.bukkit.configuration.ConfigurationSection
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.addon.AddonProgress
 *
 * @author sky
 * @since 2021/3/5 11:14 上午
 */
@Id("progress")
class AddonProgress(config: ConfigurationSection, questContainer: QuestContainer) : Addon(config, questContainer) {

    val value = config.get("value", "null")!!.asList()
    val target = config.get("target", "null")!!.asList()
    val percent = config.get("percent", "null")!!.asList()

    fun getProgress(profile: PlayerProfile): CompletableFuture<Progress> {
        val future = CompletableFuture<Progress>()
        val task = questContainer as? Task
        if (task == null) {
            warning("Template(${questContainer.path}) not support addon(Stats).")
            future.complete(Progress.empty)
            return future
        }
        val quest = profile.quests.firstOrNull { it.id == task.template.id }
        if (quest == null) {
            warning("Quest(${questContainer.node}) not accepted.")
            future.complete(Progress.empty)
            return future
        }
        KetherShell.eval(value, namespace = namespaceQuest) {
            sender = profile.player
            rootFrame().variables().also { vars ->
                vars.set("@Quest", quest)
                vars.set("@QuestContainer", task)
            }
        }.thenAccept { value ->
            KetherShell.eval(target, namespace = namespaceQuest) {
                sender = profile.player
                rootFrame().variables().also { vars ->
                    vars.set("@Quest", quest)
                    vars.set("@QuestContainer", task)
                }
            }.thenAccept { target ->
                KetherShell.eval(percent, namespace = namespaceQuest) {
                    sender = profile.player
                    rootFrame().variables().also { vars ->
                        vars.set("@Quest", quest)
                        vars.set("@QuestContainer", task)
                    }
                }.thenAccept { percent ->
                    val p = task.objective.getProgress(profile, task)
                    future.complete(Progress(value ?: p.value, target ?: p.target, Coerce.toDouble(percent ?: p.percent)))
                }
            }
        }
        return future
    }

    companion object {

        fun Task.progressAddon() = addon<AddonProgress>("progress")
    }
}