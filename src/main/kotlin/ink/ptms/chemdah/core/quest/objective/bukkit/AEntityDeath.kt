package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Abstract
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.event.entity.EntityDeathEvent
import taboolib.common.reflect.Reflex.Companion.invokeMethod

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.AEntityDeath
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Abstract
abstract class AEntityDeath<T : EntityDeathEvent> : ObjectiveCountableI<T>() {

    init {
        addSimpleCondition("position") { e ->
            toPosition().inside(e.entity.location)
        }
        addSimpleCondition("damage") { e ->
            toInt() <= (e.entity.lastDamageCause?.damage ?: 0.0)
        }
        addSimpleCondition("damage:final") { e ->
            toInt() <= (e.entity.lastDamageCause?.finalDamage ?: 0.0)
        }
        addSimpleCondition("cause") { e ->
            asList().any { it.equals(e.entity.lastDamageCause?.cause?.name.toString(), true) }
        }
        addSimpleCondition("drops") { e ->
            e.drops.any { toInferItem().isItem(it) }
        }
        addSimpleCondition("exp") { e ->
            toInt() <= e.droppedExp
        }
        addSimpleCondition("revive-health") { e ->
            toInt() <= e.invokeMethod<Int>("getReviveHealth")!!
        }
        addConditionVariable("damage") { e ->
            e.entity.lastDamageCause?.damage ?: 0.0
        }
        addConditionVariable("damage:final") { e ->
            e.entity.lastDamageCause?.finalDamage ?: 0.0
        }
        addConditionVariable("exp") { e ->
            e.droppedExp
        }
        addConditionVariable("revive-health") { e ->
            e.invokeMethod<Double>("getReviveHealth")!!
        }
    }
}