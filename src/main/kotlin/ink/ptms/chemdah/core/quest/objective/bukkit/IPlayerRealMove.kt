package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.event.player.PlayerMoveEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerRealMove
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerRealMove : ObjectiveCountableI<PlayerMoveEvent>() {

    override val name = "player real move"
    override val event = PlayerMoveEvent::class.java
    override val isAsync = true

    init {
        handler {
            if (from.x.toInt() != to!!.x.toInt() || from.z.toInt() != to!!.z.toInt()) player else null
        }
        addSimpleCondition("position") { e ->
            toPosition().inside(e.to!!)
        }
        addSimpleCondition("position:to") { e ->
            toPosition().inside(e.to!!)
        }
        addSimpleCondition("position:from") { e ->
            toPosition().inside(e.from)
        }
    }
}