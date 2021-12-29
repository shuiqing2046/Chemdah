package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.event.player.PlayerCommandPreprocessEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerCommand
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerCommand : ObjectiveCountableI<PlayerCommandPreprocessEvent>() {

    override val name = "player command"
    override val event = PlayerCommandPreprocessEvent::class.java

    init {
        handler {
            player
        }
        addSimpleCondition("position") { e ->
            toPosition().inside(e.player.location)
        }
        addSimpleCondition("command") { e ->
            e.message.startsWith(toString(), true)
        }
        addConditionVariable("command") {
            it.message
        }
    }
}