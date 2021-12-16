package ink.ptms.chemdah.core.quest.objective.other

import ink.ptms.chemdah.core.quest.objective.Objective
import org.bukkit.event.Event

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.other.INever
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
object INever : Objective<Event>() {

    override val name = "never"
    override val event = Event::class
    override val isListener = false

    init {
        addGoal("null") { _, _ -> false }
    }
}