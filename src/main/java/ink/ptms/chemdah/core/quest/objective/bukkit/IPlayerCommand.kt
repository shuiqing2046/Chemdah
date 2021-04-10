package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountable
import org.bukkit.event.player.PlayerCommandPreprocessEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerCommand
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerCommand : ObjectiveCountable<PlayerCommandPreprocessEvent>() {

    override val name = "player command"
    override val event = PlayerCommandPreprocessEvent::class

    init {
        handler {
            player
        }
        addCondition("position") { e ->
            toPosition().inside(e.player.location)
        }
        addCondition("command") { e ->
            e.message.startsWith(toString(), true)
        }
        addConditionVariable("command") {
            it.message
        }
    }
}