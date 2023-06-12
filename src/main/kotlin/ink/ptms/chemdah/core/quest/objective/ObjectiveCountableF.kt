package ink.ptms.chemdah.core.quest.objective

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Quest
import ink.ptms.chemdah.core.quest.Task
import ink.ptms.chemdah.core.quest.objective.Progress.Companion.toProgress
import taboolib.common5.cdouble

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.ObjectiveCountable2
 *
 * @author sky
 * @since 2021/3/1 11:53 下午
 */
@Abstract
abstract class ObjectiveCountableF<E : Any> : Objective<E>() {

    init {
        addGoal("amount") { profile, task ->
            profile.dataOperator(task) {
                get("amount", 0).cdouble >= task.goal["amount", 1].cdouble
            }
        }
        addGoalVariable("amount") { profile, task ->
            profile.dataOperator(task) {
                get("amount", 0).cdouble
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onContinue(profile: PlayerProfile, task: Task, quest: Quest, event: Any) {
        profile.dataOperator(task) {
            put("amount", get("amount", 0).cdouble + getCount(profile, task, event as E))
        }
    }

    override fun getProgress(profile: PlayerProfile, task: Task): Progress {
        val target = task.goal["amount", 1].cdouble
        return if (hasCompletedSignature(profile, task)) {
            target.toProgress(target, 1.0)
        } else {
            profile.dataOperator(task) { get("amount", 0).cdouble.toProgress(target) }
        }
    }

    open fun getCount(profile: PlayerProfile, task: Task, event: E): Double {
        return 1.0
    }
}