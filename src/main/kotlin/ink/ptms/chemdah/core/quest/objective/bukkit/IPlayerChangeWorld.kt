package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.event.player.PlayerChangedWorldEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerChangeWorld
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerChangeWorld : ObjectiveCountableI<PlayerChangedWorldEvent>() {

    override val name = "change world"
    override val event = PlayerChangedWorldEvent::class.java

    init {
        handler {
            it.player
        }
        addSimpleCondition("position") { data, e ->
            data.toPosition().inside(e.player.location)
        }
        addSimpleCondition("world") { data, e ->
            data.asList().any { it.equals(e.player.world.name, true) }
        }
        addSimpleCondition("world:to") { data, e ->
            data.asList().any { it.equals(e.player.world.name, true) }
        }
        addSimpleCondition("world:from") { data, e ->
            data.asList().any { it.equals(e.from.name, true) }
        }
    }
}