package ink.ptms.chemdah.core.quest.addon

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.api.ChemdahAPI.chemdahProfile
import ink.ptms.chemdah.api.event.collect.ObjectiveEvents
import ink.ptms.chemdah.core.quest.*
import org.bukkit.entity.Player
import taboolib.common.platform.event.SubscribeEvent

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

    val depend = root.split("[,;]".toRegex()).flatMap {
        // 任务组
        if (it.startsWith("group:")) {
            // 从任务组中获取所有任务名称
            ChemdahAPI.getQuestTemplateGroup(root.substringAfter("group:"))?.quests?.map { q -> q.id } ?: emptyList()
        } else {
            listOf(it)
        }
    }

    companion object {

        /** 获取依赖组件 */
        fun QuestContainer.depend() = addon<AddonDepend>("depend")?.depend

        /** 检查任务依赖是否完成 */
        fun QuestContainer.isQuestDependCompleted(player: Player): Boolean {
            when (this) {
                // 是任务，只能依赖任务
                is Template -> {
                    val depends = depend() ?: return true
                    return depends.all { player.chemdahProfile.isQuestCompleted(it) }
                }
                // 是条目，可以依赖任务或条目
                is Task -> {
                    val depends = depend() ?: return true
                    val tasks = template.taskMap
                    return depends.all {
                        // 是当前任务中的条目
                        if (tasks.containsKey(it)) {
                            // 检查条目是否完成
                            tasks[it]!!.isCompleted(player.chemdahProfile)
                        } else {
                            player.chemdahProfile.isQuestCompleted(it)
                        }
                    }
                }
            }
            return true
        }

        @SubscribeEvent
        private fun onObjectiveContinuePre(e: ObjectiveEvents.Continue.Pre) {
            if (!e.task.isQuestDependCompleted(e.playerProfile.player)) {
                e.isCancelled = true
            }
        }

        @SubscribeEvent
        private fun onObjectiveCompletePre(e: ObjectiveEvents.Complete.Pre) {
            if (!e.task.isQuestDependCompleted(e.playerProfile.player)) {
                e.isCancelled = true
            }
        }
    }
}