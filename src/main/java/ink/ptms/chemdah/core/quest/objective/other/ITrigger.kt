package ink.ptms.chemdah.core.quest.objective.other

import ink.ptms.chemdah.core.quest.Task
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountable
import org.bukkit.event.Event

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.other.ITrigger
 *
 * task:0:
 *   objective: trigger
 *   condition:
 *      value: def
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
object ITrigger : ObjectiveCountable<Event>() {

    override val name = "trigger"
    override val event = Event::class
    override val isListener = false

    init {
        addCondition { profile, task, _ ->
            !task.condition.containsKey("position") || task.condition["position"]!!.toPosition().inside(profile.player.location)
        }
    }

    fun getValue(task: Task): String? {
        return task.condition["value"]?.toString()
    }
}