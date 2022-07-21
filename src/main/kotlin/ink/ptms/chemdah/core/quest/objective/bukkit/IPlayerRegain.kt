package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Task
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableF
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityRegainHealthEvent
import taboolib.library.reflex.Reflex.Companion.invokeMethod

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
    override val event = EntityRegainHealthEvent::class.java

    init {
        handler {
            it.entity as? Player
        }
        addSimpleCondition("position") { data, e ->
            data.toPosition().inside(e.entity.location)
        }
        addSimpleCondition("amount") { data, e ->
            data.toInt() <= e.amount
        }
        addSimpleCondition("reason") { data, e ->
            data.asList().any { it.equals(e.regainReason.name, true) }
        }
        addSimpleCondition("fast") { data, e ->
            data.toBoolean() == e.invokeMethod("isFastRegen")
        }
        addConditionVariable("amount") {
            it.amount
        }
    }

    override fun getCount(profile: PlayerProfile, task: Task, event: EntityRegainHealthEvent): Double {
        return event.amount
    }
}