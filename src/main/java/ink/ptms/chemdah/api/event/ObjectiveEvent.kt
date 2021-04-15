package ink.ptms.chemdah.api.event

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Task
import ink.ptms.chemdah.core.quest.objective.Objective
import io.izzel.taboolib.module.event.EventNormal

/**
 * Chemdah
 * ink.ptms.chemdah.api.event.ObjectiveEvent
 *
 * @author sky
 * @since 2021/2/21 1:07 上午
 */
class ObjectiveEvent {

    /**
     * 当条目继续时
     */
    class Continue(val objective: Objective<*>, val task: Task, val playerProfile: PlayerProfile): EventNormal<Continue>(true) {

        fun isCompleted(): Boolean {
            return task.isCompleted(playerProfile) || task.getQuest(playerProfile)?.isCompleted ?: true
        }
    }

    /**
     * 当条目完成时
     */
    class Complete(val objective: Objective<*>, val task: Task, val playerProfile: PlayerProfile): EventNormal<Complete>(true)

    /**
     * 当条目重置时
     */
    class Reset(val objective: Objective<*>, val task: Task, val playerProfile: PlayerProfile): EventNormal<Reset>(true)
}