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
    override val event = PlayerRespawnEvent::class.java

    init {
        handler {
            it.player
        }
        addSimpleCondition("position") { data, e ->
            data.toPosition().inside(e.respawnLocation)
        }
        addSimpleCondition("spawn:bed") { data, e ->
            data.toBoolean() == e.isBedSpawn
        }
        addSimpleCondition("spawn:anchor") { data, e ->
            data.toBoolean() == e.isAnchorSpawn
        }
    }
}