package ink.ptms.chemdah.core.quest.addon

import ink.ptms.chemdah.api.event.collect.ObjectiveEvents
import ink.ptms.chemdah.core.quest.Id
import ink.ptms.chemdah.core.quest.Option
import ink.ptms.chemdah.core.quest.QuestContainer
import ink.ptms.chemdah.util.asList
import io.izzel.taboolib.module.inject.TListener
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.addon.AddonDepend
 *
 * @author sky
 * @since 2021/3/4 9:04 上午
 */
@Id("depend")
@Option(Option.Type.TEXT)
class AddonDepend(root: String, questContainer: QuestContainer) : Addon(root, questContainer) {

    val depend = root.asList()

    @TListener
    companion object : Listener {

        fun QuestContainer.depend() = addon<AddonDepend>("depend")?.depend

        @EventHandler
        fun e(e: ObjectiveEvents.Continue.Pre) {
            val taskDepend = e.task.depend()
            if (taskDepend != null) {
                val taskMap = e.task.template.taskMap
                if (taskDepend.any { taskMap[it]?.isCompleted(e.playerProfile) != true }) {
                    e.isCancelled = true
                }
            }
            val questDepend = e.task.template.depend()
            if (questDepend != null) {
                if (questDepend.any { !e.playerProfile.isQuestCompleted(it) }) {
                    e.isCancelled = true
                }
            }
        }
    }
}