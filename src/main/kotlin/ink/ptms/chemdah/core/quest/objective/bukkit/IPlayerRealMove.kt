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
            if (it.from.x.toInt() != it.to!!.x.toInt() || it.from.z.toInt() != it.to!!.z.toInt()) it.player else null
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