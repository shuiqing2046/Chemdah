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
    override val event = PlayerChangedWorldEvent::class

    init {
        handler {
            player
        }
        addCondition("position") { e ->
            toPosition().inside(e.player.location)
        }
        addCondition("world") { e ->
            asList().any { it.equals(e.player.world.name, true) }
        }
        addCondition("world:to") { e ->
            asList().any { it.equals(e.player.world.name, true) }
        }
        addCondition("world:from") { e ->
            asList().any { it.equals(e.from.name, true) }
        }
    }
}