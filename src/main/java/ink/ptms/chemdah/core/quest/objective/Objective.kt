package ink.ptms.chemdah.core.quest.objective

import ink.ptms.chemdah.core.quest.PlayerProfile
import ink.ptms.chemdah.core.quest.Task
import org.bukkit.event.Event
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.Objective
 *
 * @author sky
 * @since 2021/3/1 11:52 下午
 */
abstract class Objective {

    private val conditions = ArrayList<(PlayerProfile, Task, Event) -> Boolean>()
    private val goals = ArrayList<(PlayerProfile, Task) -> Boolean>()

    abstract val name: String
    abstract val event: KClass<out Event>

    open fun next(profile: PlayerProfile, task: Task, event: Event) {
    }

    open fun reset(profile: PlayerProfile, task: Task) {
    }

    open fun finish(profile: PlayerProfile, task: Task) {
    }

    open fun checkCondition(profile: PlayerProfile, task: Task, event: Event): CompletableFuture<Boolean> {
        return if (conditions.all { it(profile, task, event) }) {
            profile.checkAgent(task.condition["$"]?.value, event)
        } else {
            CompletableFuture.completedFuture(false)
        }
    }

    open fun checkGoal(profile: PlayerProfile, task: Task): CompletableFuture<Boolean> {
        return if (goals.all { it(profile, task) }) {
            profile.checkAgent(task.goal["$"]?.value)
        } else {
            CompletableFuture.completedFuture(false)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <E : Event> checkCondition(func: (PlayerProfile, Task, E) -> Boolean) {
        conditions += func as (PlayerProfile, Task, Event) -> Boolean
    }

    fun checkGoal(func: (PlayerProfile, Task) -> Boolean) {
        goals += func
    }
}