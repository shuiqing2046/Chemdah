package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.event.player.PlayerBedEnterEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerBedEnter
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerBedEnter : ObjectiveCountableI<PlayerBedEnterEvent>() {

    override val name = "bed enter"
    override val event = PlayerBedEnterEvent::class

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
        addCondition("reason") { e ->
            asList().any { it.equals(e.bedEnterResult.name, true) }
        }
    }
}