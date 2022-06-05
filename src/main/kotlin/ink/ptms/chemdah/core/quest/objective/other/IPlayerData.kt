package ink.ptms.chemdah.core.quest.objective.other

import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.event.Event

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.other.ICustomLevel
 *
 * task:0:
 *   objective: player data
 *   goal:
 *      key: def
 *      value: 10
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
object IPlayerData : ObjectiveCountableI<Event>() {

    override val name = "player data"
    override val event = Event::class.java
    override val isListener = false
    override val isTickable = true

    init {
        addGoal("key,value") { profile, task ->
            profile.persistentDataContainer[task.goal["key"].toString()].toString() == task.goal["value"].toString()
        }
    }
}