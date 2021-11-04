package ink.ptms.chemdah.core.quest.addon

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.api.ChemdahAPI.chemdahProfile
import ink.ptms.chemdah.api.event.collect.ObjectiveEvents
import ink.ptms.chemdah.core.quest.Id
import ink.ptms.chemdah.core.quest.Option
import ink.ptms.chemdah.core.quest.QuestContainer
import ink.ptms.chemdah.core.quest.Task
import org.bukkit.entity.Player
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.util.asList

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.addon.AddonDepend
 *
 * addon:
 *   depend: quest
 *   depend: group:quest
 *
 * @author sky
 * @since 2021/3/4 9:04 上午
 */
@Id("depend")
@Option(Option.Type.TEXT)
class AddonDepend(root: String, questContainer: QuestContainer) : Addon(root, questContainer) {

    val depend = if (root.startsWith("group:")) {
        ChemdahAPI.getQuestTemplateGroup(root)?.group?.map { it.id }?.toList()
    } else {
        root.asList()
    }

    companion object {

        fun QuestContainer.depend() = addon<AddonDepend>("depend")?.depend

        fun QuestContainer.isQuestDependCompleted(player: Player): Boolean {
            // 任务依赖
            val questDepend = if (this is Task) template.depend() else depend()
            if (questDepend != null) {
                if (questDepend.any { !player.chemdahProfile.isQuestCompleted(it) }) {
                    return false
                }
            }
            // 条目依赖
            if (this is Task) {
                val taskDepend = depend()
                if (taskDepend != null) {
                    val taskMap = template.taskMap
                    if (taskDepend.any { taskMap[it]?.isCompleted(player.chemdahProfile) != true }) {
                        return false
                    }
                }
            }
            return true
        }

        @SubscribeEvent
        fun e(e: ObjectiveEvents.Continue.Pre) {
            if (!e.task.isQuestDependCompleted(e.playerProfile.player)) {
                e.isCancelled = true
            }
        }

        @SubscribeEvent
        fun e(e: ObjectiveEvents.Complete.Pre) {
            if (!e.task.isQuestDependCompleted(e.playerProfile.player)) {
                e.isCancelled = true
            }
        }
    }
}