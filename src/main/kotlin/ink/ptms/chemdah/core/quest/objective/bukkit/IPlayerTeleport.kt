package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.event.player.PlayerTeleportEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerTeleport
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerTeleport : ObjectiveCountableI<PlayerTeleportEvent>() {

    override val name = "player teleport"
    override val event = PlayerTeleportEvent::class.java
    override val isAsync = true

    init {
        handler {
            it.player
        }
        addSimpleCondition("position") { data, e ->
            data.toPosition().inside(e.to!!)
        }
        addSimpleCondition("position:to") { data, e ->
            data.toPosition().inside(e.to ?: EMPTY_LOCATION)
        }
        addSimpleCondition("position:from") { data, e ->
            data.toPosition().inside(e.from)
        }
        addSimpleCondition("cause") { data, e ->
            data.asList().any { it.equals(e.cause.name, true) }
        }
    }
}