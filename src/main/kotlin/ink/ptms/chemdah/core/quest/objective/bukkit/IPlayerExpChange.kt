package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Task
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.entity.Entity
import org.bukkit.event.player.PlayerExpChangeEvent
import taboolib.common.reflect.Reflex.Companion.invokeMethod

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerExpChange
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerExpChange : ObjectiveCountableI<PlayerExpChangeEvent>() {

    override val name = "exp change"
    override val event = PlayerExpChangeEvent::class.java

    init {
        handler {
            player
        }
        addSimpleCondition("position") { e ->
            toPosition().inside(e.player.location)
        }
        addSimpleCondition("amount") { e ->
            toInt() <= e.amount
        }
        addSimpleCondition("entity") { e ->
            toInferEntity().isEntity(e.invokeMethod<Entity>("getSource"))
        }
        addConditionVariable("amount") {
            it.amount
        }
    }

    override fun getCount(profile: PlayerProfile, task: Task, event: PlayerExpChangeEvent): Int {
        return event.amount
    }
}