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
    override val event = PlayerTeleportEvent::class
    override val isAsync = true

    init {
        handler {
            player
        }
        addCondition("position") { e ->
            toPosition().inside(e.to)
        }
        addCondition("position:to") { e ->
            toPosition().inside(e.to ?: EMPTY)
        }
        addCondition("position:from") { e ->
            toPosition().inside(e.from)
        }
        addCondition("cause") { e ->
            asList().any { it.equals(e.cause.name, true) }
        }
    }
}