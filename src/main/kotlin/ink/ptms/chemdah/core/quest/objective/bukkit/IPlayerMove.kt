package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.event.player.PlayerMoveEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerMove
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerMove : ObjectiveCountableI<PlayerMoveEvent>() {

    override val name = "player move"
    override val event = PlayerMoveEvent::class.java
    override val isAsync = true

    init {
        handler {
            if (it.from.x != it.to!!.x || it.from.z != it.to!!.z) it.player else null
        }
        addSimpleCondition("position") { data, e ->
            data.toPosition().inside(e.to!!)
        }
        addSimpleCondition("position:to") { data, e ->
            data.toPosition().inside(e.to!!)
        }
        addSimpleCondition("position:from") { data, e ->
            data.toPosition().inside(e.from)
        }
    }
}