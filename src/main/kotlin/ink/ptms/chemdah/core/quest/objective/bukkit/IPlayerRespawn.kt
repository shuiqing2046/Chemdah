package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.event.player.PlayerRespawnEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerRespawn
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerRespawn : ObjectiveCountableI<PlayerRespawnEvent>() {

    override val name = "player respawn"
    override val event = PlayerRespawnEvent::class

    init {
        handler {
            player
        }
        addCondition("position") { e ->
            toPosition().inside(e.respawnLocation)
        }
        addCondition("spawn:bed") { e ->
            toBoolean() == e.isBedSpawn
        }
        addCondition("spawn:anchor") { e ->
            toBoolean() == e.isAnchorSpawn
        }
    }
}