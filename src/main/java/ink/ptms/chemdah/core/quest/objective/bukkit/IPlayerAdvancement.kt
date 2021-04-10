package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountable
import org.bukkit.event.player.PlayerAdvancementDoneEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerAdvancement
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerAdvancement : ObjectiveCountable<PlayerAdvancementDoneEvent>() {

    override val name = "player advancement"
    override val event = PlayerAdvancementDoneEvent::class

    init {
        handler {
            player
        }
        addCondition("position") { e ->
            toPosition().inside(e.player.location)
        }
        addCondition("advancement") { e ->
            asList().any { it.equals(e.advancement.key.toString(), true) }
        }
    }
}