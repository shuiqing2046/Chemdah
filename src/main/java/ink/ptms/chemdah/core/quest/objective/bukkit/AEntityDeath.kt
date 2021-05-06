package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Abstract
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.event.entity.EntityDeathEvent

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
        addCondition("position") { e ->
            toPosition().inside(e.entity.location)
        }
        addCondition("damage") { e ->
            toInt() <= e.entity.lastDamageCause?.damage ?: 0.0
        }
        addCondition("damage:final") { e ->
            toInt() <= e.entity.lastDamageCause?.finalDamage ?: 0.0
        }
        addCondition("cause") { e ->
            asList().any { it.equals(e.entity.lastDamageCause?.cause?.name.toString(), true) }
        }
        addCondition("drops") { e ->
            e.drops.any { toInferItem().isItem(it) }
        }
        addCondition("exp") { e ->
            toInt() <= e.droppedExp
        }
        addCondition("revive-health") { e ->
            toInt() <= e.reviveHealth
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
            e.reviveHealth
        }
    }
}