package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.api.event.collect.ConversationEvents
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import taboolib.platform.util.attacker

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerAttack
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerConversation : ObjectiveCountableI<ConversationEvents.ReplyClosed>() {

    override val name = "player conversation"
    override val event = ConversationEvents.ReplyClosed::class
    override val isAsync = true

    init {
        handler {
            session.player
        }
        addCondition("position") { e ->
            toPosition().inside(e.session.origin)
        }
        addCondition("id") { e ->
            asList().any { it.equals(e.session.conversation.id, true) }
        }
    }
}