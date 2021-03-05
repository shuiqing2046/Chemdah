package ink.ptms.chemdah.core.quest.addon

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Id
import ink.ptms.chemdah.core.quest.Task
import ink.ptms.chemdah.core.quest.objective.Progress
import ink.ptms.chemdah.core.script.namespaceQuest
import ink.ptms.chemdah.util.asList
import io.izzel.taboolib.kotlin.kether.KetherShell
import io.izzel.taboolib.util.Coerce
import org.bukkit.configuration.ConfigurationSection
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.addon.AddonStats
 *
 * @author sky
 * @since 2021/3/5 11:14 上午
 */
@Id("stats")
class AddonStats(config: ConfigurationSection, task: Task) : Addon(config, task) {

    val value = config.get("value", "*0")!!.asList()
    val target = config.get("target", "*0")!!.asList()
    val percent = config.get("percent", "*0")!!.asList()

    fun getProgress(profile: PlayerProfile): CompletableFuture<Progress> {
        val future = CompletableFuture<Progress>()
        val quest = profile.quests.firstOrNull { it.id == task.template.id }
        if (quest == null) {
            future.complete(null)
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
                    future.complete(Progress(value ?: 0, target ?: 0, Coerce.toDouble(percent)))
                }
            }
        }
        return future
    }

    companion object {

        fun Task.stats() = addon<AddonStats>("stats")
    }
}