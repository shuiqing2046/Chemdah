package ink.ptms.chemdah.core.quest.objective.other

import ink.ptms.chemdah.core.quest.Task
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
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
object ITrigger : ObjectiveCountableI<Event>() {

    override val name = "trigger"
    override val event = Event::class
    override val isListener = false

    init {
        addFullCondition("position") { profile, task, _ ->
            task.condition["position"]!!.toPosition().inside(profile.player.location)
        }
    }

    fun getValues(task: Task): List<String> {
        return (task.condition["value"] ?: task.condition["values"])?.asList() ?: emptyList()
    }
}