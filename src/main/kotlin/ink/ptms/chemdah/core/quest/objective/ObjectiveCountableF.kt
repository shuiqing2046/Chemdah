package ink.ptms.chemdah.core.quest.objective

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Quest
import ink.ptms.chemdah.core.quest.Task
import ink.ptms.chemdah.core.quest.objective.Progress.Companion.progress
import taboolib.common5.Coerce
import org.bukkit.event.Event

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
        addGoal { profile, task ->
            profile.dataOperator(task) {
                get("amount", 0).toDouble() >= task.goal["amount", 1].toDouble()
            }
        }
        addGoalVariable("amount") { profile, task ->
            profile.dataOperator(task) {
                get("amount", 0).toDouble()
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onContinue(profile: PlayerProfile, task: Task, quest: Quest, event: Any) {
        profile.dataOperator(task) {
            put("amount", get("amount", 0).toDouble() + getCount(profile, task, event as E))
        }
    }

    override fun getProgress(profile: PlayerProfile, task: Task): Progress {
        val target = Coerce.format(task.goal["amount", 1].toDouble())
        return if (hasCompletedSignature(profile, task)) {
            target.progress(target, 1.0)
        } else {
            profile.dataOperator(task) {
                get("amount", 0).toDouble().let { a -> Coerce.format(a).progress(target, Coerce.format(a / target)) }
            }
        }
    }

    open fun getCount(profile: PlayerProfile, task: Task, event: E): Double {
        return 1.0
    }
}