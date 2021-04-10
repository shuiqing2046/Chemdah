package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountable
import org.bukkit.event.player.PlayerBedLeaveEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerBedLeave
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerBedLeave : ObjectiveCountable<PlayerBedLeaveEvent>() {

    override val name = "bed leave"
    override val event = PlayerBedLeaveEvent::class

    init {
        handler {
            player
        }
        addCondition("position") { e ->
            toPosition().inside(e.bed.location)
        }
        addCondition("bed") { e ->
            toInferBlock().isBlock(e.bed)
        }
    }
}