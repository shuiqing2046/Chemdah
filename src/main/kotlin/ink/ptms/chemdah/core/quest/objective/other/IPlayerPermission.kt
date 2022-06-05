package ink.ptms.chemdah.core.quest.objective.other

import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.event.Event
import taboolib.platform.util.hasItem
import taboolib.platform.util.takeItem

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.other.IPlayerPermission
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
object IPlayerPermission : ObjectiveCountableI<Event>() {

    override val name = "player permission"
    override val event = Event::class.java
    override val isListener = false
    override val isTickable = true

    init {
        addGoal("permission") { profile, task ->
            profile.player.hasPermission(task.condition["permission"].toString())
        }
    }
}