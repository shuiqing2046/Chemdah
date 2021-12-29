package ink.ptms.chemdah.core.quest.objective.other

import ink.ptms.chemdah.core.quest.objective.Objective
import org.bukkit.event.Event

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.other.IAlways
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
object IAlways : Objective<Event>() {

    override val name = "always"
    override val event = Event::class.java
    override val isListener = false

    init {
        addFullCondition("position") { profile, task, _ ->
            task.condition["position"]!!.toPosition().inside(profile.player.location)
        }
    }
}