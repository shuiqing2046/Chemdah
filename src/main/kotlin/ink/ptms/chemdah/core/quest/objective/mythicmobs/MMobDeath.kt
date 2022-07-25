package ink.ptms.chemdah.core.quest.objective.mythicmobs

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import ink.ptms.um.event.MobDeathEvent
import org.bukkit.entity.Player

@Dependency("MythicMobs")
object MMobDeath : ObjectiveCountableI<MobDeathEvent>() {

    override val name = "mythicmobs kill"
    override val event = MobDeathEvent::class.java

    init {
        handler {
            it.killer as? Player
        }
        addSimpleCondition("position") { data, e ->
            data.toPosition().inside(e.killer!!.location)
        }
        addSimpleCondition("name") { data, e ->
            data.asList().any { it.equals(e.mob.id, true) }
        }
        addSimpleCondition("level") { data, e ->
            data.toDouble() == e.mob.level
        }
        addSimpleCondition("min-level") { data, e ->
            data.toDouble() <= e.mob.level
        }
        addConditionVariable("name") {
            it.mob.id
        }
    }
}
