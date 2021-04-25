package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Quest
import ink.ptms.chemdah.core.quest.Task
import ink.ptms.chemdah.core.quest.objective.Abstract
import ink.ptms.chemdah.core.quest.objective.Objective
import ink.ptms.chemdah.core.quest.objective.Progress
import ink.ptms.chemdah.core.quest.objective.Progress.Companion.progress
import io.izzel.taboolib.util.Coerce
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDamageEvent

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
        addCondition("position") { e ->
            toPosition().inside(e.entity.location)
        }
        addCondition("victim") { e ->
            toInferEntity().isEntity(e.entity)
        }
        addCondition("damage") { e ->
            toInt() <= e.damage
        }
        addCondition("damage:final") { e ->
            toInt() <= e.finalDamage
        }
        addCondition("cause") { e ->
            asList().any { it.equals(e.cause.name, true) }
        }
        addConditionVariable("damage") {
            it.damage
        }
        addConditionVariable("damage:final") {
            it.finalDamage
        }
        addGoal { profile, task ->
            profile.dataOperator(task) {
                get("damage", 0).toDouble() >= task.goal["damage", 1].toDouble()
            }
        }
        addGoalVariable("damage") { profile, task ->
            profile.dataOperator(task) {
                get("damage", 0).toDouble()
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onContinue(profile: PlayerProfile, task: Task, quest: Quest, event: Event) {
        super.onContinue(profile, task, quest, event)
        profile.dataOperator(task) {
            put("damage", get("damage", 0).toDouble() + getDamage(profile, task, event as E))
        }
    }

    override fun getProgress(profile: PlayerProfile, task: Task): Progress {
        val target = Coerce.format(task.goal["damage", 1].toDouble())
        return if (hasCompletedSignature(profile, task)) {
            target.progress(target, 1.0)
        } else {
            profile.dataOperator(task) {
                get("damage", 0).toDouble().let { a -> Coerce.format(a).progress(target, Coerce.format(a / target)) }
            }
        }
    }

    open fun getDamage(profile: PlayerProfile, task: Task, event: E): Double {
        return event.finalDamage
    }
}