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
        addSimpleCondition("position") { data, e ->
            data.toPosition().inside(e.entity.location)
        }
        addSimpleCondition("damage") { data, e ->
            data.toInt() <= (e.entity.lastDamageCause?.damage ?: 0.0)
        }
        addSimpleCondition("damage:final") { data, e ->
            data.toInt() <= (e.entity.lastDamageCause?.finalDamage ?: 0.0)
        }
        addSimpleCondition("cause") { data, e ->
            data.asList().any { it.equals(e.entity.lastDamageCause?.cause?.name.toString(), true) }
        }
        addSimpleCondition("drops") { data, e ->
            e.drops.any { data.toInferItem().isItem(it) }
        }
        addSimpleCondition("exp") { data, e ->
            data.toInt() <= e.droppedExp
        }
        addSimpleCondition("revive-health") { data, e ->
            data.toInt() <= e.invokeMethod<Int>("getReviveHealth")!!
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