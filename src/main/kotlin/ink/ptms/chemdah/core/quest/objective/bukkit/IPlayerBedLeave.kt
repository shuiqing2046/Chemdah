package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.event.player.PlayerBedLeaveEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerBedLeave
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerBedLeave : ObjectiveCountableI<PlayerBedLeaveEvent>() {

    override val name = "bed leave"
    override val event = PlayerBedLeaveEvent::class.java

    init {
        handler {
            it.player
        }
        addSimpleCondition("position") { data, e ->
            data.toPosition().inside(e.bed.location)
        }
        addSimpleCondition("bed") { data, e ->
            data.toInferBlock().isBlock(e.bed)
        }
    }
}