package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Task
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableF
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityRegainHealthEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerRegain
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerRegain : ObjectiveCountableF<EntityRegainHealthEvent>() {

    override val name = "health regain"
    override val event = EntityRegainHealthEvent::class

    init {
        handler {
            entity as? Player
        }
        addCondition("position") { e ->
            toPosition().inside(e.entity.location)
        }
        addCondition("amount") { e ->
            toInt() <= e.amount
        }
        addCondition("reason") { e ->
            asList().any { it.equals(e.regainReason.name, true) }
        }
        addCondition("fast") { e ->
            toBoolean() == e.isFastRegen
        }
        addConditionVariable("amount") {
            it.amount
        }
    }

    override fun getCount(profile: PlayerProfile, task: Task, event: EntityRegainHealthEvent): Double {
        return event.amount
    }
}