package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.event.player.PlayerAdvancementDoneEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerAdvancement
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerAdvancement : ObjectiveCountableI<PlayerAdvancementDoneEvent>() {

    override val name = "player advancement"
    override val event = PlayerAdvancementDoneEvent::class.java

    init {
        handler {
            player
        }
        addSimpleCondition("position") { e ->
            toPosition().inside(e.player.location)
        }
        addSimpleCondition("advancement") { e ->
            asList().any { it.equals(e.advancement.key.toString(), true) }
        }
    }
}