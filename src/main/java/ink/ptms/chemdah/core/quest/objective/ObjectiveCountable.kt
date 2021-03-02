package ink.ptms.chemdah.core.quest.objective

import ink.ptms.chemdah.core.quest.PlayerProfile
import ink.ptms.chemdah.core.quest.Task
import org.bukkit.event.Event

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.OCountable
 *
 * @author sky
 * @since 2021/3/1 11:53 下午
 */
abstract class ObjectiveCountable : Objective() {

    init {
        checkGoal { profile, task ->
            profile.metadata(task) {
                get("amount", 0).toInt() >= task.goal.get("amount", 1).toInt()
            }
        }
    }

    override fun next(profile: PlayerProfile, task: Task, event: Event) {
        profile.metadata(task) {
            put("amount", get("amount", 0).toInt() + 1)
        }
    }

    override fun reset(profile: PlayerProfile, task: Task) {
        profile.metadata(task) {
            remove("amount")
        }
    }

    override fun finish(profile: PlayerProfile, task: Task) {
        profile.metadata(task) {
            put("amount", task.goal.get("amount", 1).toInt())
        }
    }
}