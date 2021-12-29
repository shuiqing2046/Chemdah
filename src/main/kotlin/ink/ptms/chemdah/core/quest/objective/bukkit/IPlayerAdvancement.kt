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
            it.player
        }
        addSimpleCondition("position") { data, e ->
            data.toPosition().inside(e.player.location)
        }
        addSimpleCondition("advancement") { data, e ->
            data.asList().any { it.equals(e.advancement.key.toString(), true) }
        }
    }
}