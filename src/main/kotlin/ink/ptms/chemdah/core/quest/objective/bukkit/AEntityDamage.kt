package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Quest
import ink.ptms.chemdah.core.quest.Task
import ink.ptms.chemdah.core.quest.objective.Abstract
import ink.ptms.chemdah.core.quest.objective.Objective
import ink.ptms.chemdah.core.quest.objective.Progress
import ink.ptms.chemdah.core.quest.objective.Progress.Companion.toProgress
import org.bukkit.event.entity.EntityDamageEvent
import taboolib.common5.Coerce
import taboolib.common5.cdouble

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.AEntityDamage
 *
 * @author sky
 * @since 2021/3/1 11:53 下午
 */
@Abstract
abstract class AEntityDamage<E : EntityDamageEvent> : Objective<E>() {

    init {
        addSimpleCondition("position") { data, e ->
            data.toPosition().inside(e.entity.location)
        }
        addSimpleCondition("victim") { data, e ->
            data.toInferEntity().isEntity(e.entity)
        }
        addSimpleCondition("damage") { data, e ->
            data.toInt() <= e.damage
        }
        addSimpleCondition("damage:final") { data, e ->
            data.toInt() <= e.finalDamage
        }
        addSimpleCondition("cause") { data, e ->
            data.asList().any { it.equals(e.cause.name, true) }
        }
        addConditionVariable("damage") {
            it.damage
        }
        addConditionVariable("damage:final") {
            it.finalDamage
        }
        addGoal("damage") { profile, task ->
            profile.dataOperator(task) {
                get("damage", 0).cdouble >= task.goal["damage", 1].cdouble
            }
        }
        addGoalVariable("damage") { profile, task ->
            profile.dataOperator(task) {
                get("damage", 0).cdouble
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onContinue(profile: PlayerProfile, task: Task, quest: Quest, event: Any) {
        profile.dataOperator(task) {
            put("damage", get("damage", 0).cdouble + getDamage(profile, task, event as E))
        }
    }

    override fun getProgress(profile: PlayerProfile, task: Task): Progress {
        val target = Coerce.format(task.goal["damage", 1].cdouble)
        return if (hasCompletedSignature(profile, task)) {
            target.toProgress(target, 1.0)
        } else {
            profile.dataOperator(task) {
                get("damage", 0).cdouble.let { a -> Coerce.format(a).toProgress(target, Coerce.format(a / target)) }
            }
        }
    }

    open fun getDamage(profile: PlayerProfile, task: Task, event: E): Double {
        return event.finalDamage
    }
}