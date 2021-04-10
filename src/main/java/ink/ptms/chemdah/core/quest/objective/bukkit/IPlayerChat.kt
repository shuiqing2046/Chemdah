package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountable
import org.bukkit.event.player.AsyncPlayerChatEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerChat
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerChat : ObjectiveCountable<AsyncPlayerChatEvent>() {

    override val name = "player chat"
    override val event = AsyncPlayerChatEvent::class

    init {
        handler {
            player
        }
        addCondition("position") { e ->
            toPosition().inside(e.player.location)
        }
        addCondition("message") { e ->
            toString() in e.message
        }
        addConditionVariable("message") {
            it.message
        }
    }
}