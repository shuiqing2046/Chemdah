package ink.ptms.chemdah.core.quest.addon

import ink.ptms.chemdah.api.event.collect.QuestEvents
import ink.ptms.chemdah.core.quest.Id
import ink.ptms.chemdah.core.quest.Option
import ink.ptms.chemdah.core.quest.Task
import ink.ptms.chemdah.core.quest.objective.other.IPlayerData
import taboolib.common.platform.event.SubscribeEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.addon.AddonResetDataOnAccepted
 *
 * @author sky
 * @since 2021/3/4 9:04 上午
 */
@Id("reset-data-on-accepted")
@Option(Option.Type.BOOLEAN)
class AddonResetDataOnAccepted(val value: Boolean, task: Task) : Addon(value, task) {

    companion object {

        /** 是否在任务接受时重置变量 */
        fun Task.isResetDataOnAccepted() = addon<AddonResetDataOnAccepted>("reset-data-on-accepted")?.value == true

        @SubscribeEvent
        private fun onAccepted(e: QuestEvents.Accept.Post) {
            e.quest.tasks.forEach { task ->
                if (task.objective is IPlayerData && task.isResetDataOnAccepted()) {
                    e.playerProfile.persistentDataContainer.remove(task.goal["key"].toString())
                }
            }
        }

        @SubscribeEvent
        private fun onAccepted(e: QuestEvents.Restart.Post) {
            e.quest.tasks.forEach { task ->
                if (task.objective is IPlayerData && task.isResetDataOnAccepted()) {
                    e.playerProfile.persistentDataContainer.remove(task.goal["key"].toString())
                }
            }
        }
    }
}