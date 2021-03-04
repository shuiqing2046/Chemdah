package ink.ptms.chemdah.core.quest.objective

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Task
import ink.ptms.chemdah.core.quest.objective.Progress.Companion.progress
import org.bukkit.event.Event

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.OCountable
 *
 * @author sky
 * @since 2021/3/1 11:53 下午
 */
abstract class ObjectiveCountable<E : Event> : Objective<E>() {

    init {
        addGoal { profile, task ->
            profile.dataOperator(task) {
                get("amount", 0).toInt() >= task.goal["amount", 1].toInt()
            }
        }
    }

    override fun onContinue(profile: PlayerProfile, task: Task, event: Event) {
        profile.dataOperator(task) {
            put("amount", get("amount", 0).toInt() + 1)
        }
    }

    override fun getProgress(profile: PlayerProfile, task: Task): Progress {
        val target = task.goal["amount", 1].toInt()
        return if (hasCompletedSignature(profile, task)) {
            target.progress(target, 1.0)
        } else {
            profile.dataOperator(task) {
                get("amount", 0).toInt().let { a -> a.progress(target, a / target.toDouble()) }
            }
        }
    }
}