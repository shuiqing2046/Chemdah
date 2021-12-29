package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.event.player.AsyncPlayerChatEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerChat
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerChat : ObjectiveCountableI<AsyncPlayerChatEvent>() {

    override val name = "player chat"
    override val event = AsyncPlayerChatEvent::class.java

    init {
        handler {
            player
        }
        addSimpleCondition("position") { e ->
            toPosition().inside(e.player.location)
        }
        addSimpleCondition("message") { e ->
            toString() in e.message
        }
        addConditionVariable("message") {
            it.message
        }
    }
}