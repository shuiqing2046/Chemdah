package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountable
import org.bukkit.event.player.PlayerTeleportEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerTeleport
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerTeleport : ObjectiveCountable<PlayerTeleportEvent>() {

    override val name = "player teleport"
    override val event = PlayerTeleportEvent::class

    init {
        handler {
            player
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("position") || task.condition["position"]!!.toPosition().inside(e.to)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("position:from") || task.condition["position:from"]!!.toPosition().inside(e.from)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("cause") || task.condition["cause"]!!.asList().any { it.equals(e.cause.name, true) }
        }
    }
}