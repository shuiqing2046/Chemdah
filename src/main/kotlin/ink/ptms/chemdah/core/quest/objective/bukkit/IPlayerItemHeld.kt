package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.event.player.PlayerItemHeldEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerItemHeld
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerItemHeld : ObjectiveCountableI<PlayerItemHeldEvent>() {

    override val name = "item held"
    override val event = PlayerItemHeldEvent::class.java

    init {
        handler {
            it.player
        }
        addSimpleCondition("position") { data, e ->
            data.toPosition().inside(e.player.location)
        }
        addSimpleCondition("slot:new") { data, e ->
            data.toInt() == e.newSlot
        }
        addSimpleCondition("slot:previous") { data, e ->
            data.toInt() == e.previousSlot
        }
    }
}