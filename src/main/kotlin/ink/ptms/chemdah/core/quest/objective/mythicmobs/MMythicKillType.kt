package ink.ptms.chemdah.core.quest.objective.mythicmobs

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent
import org.bukkit.entity.Player

@Dependency("MythicMobs")
object MMythicKillType : ObjectiveCountableI<MythicMobDeathEvent>() {

    override val name = "mythicmobs kill"
    override val event = MythicMobDeathEvent::class.java

    init {
        handler {
            killer as? Player
        }
        addSimpleCondition("position") { e ->
            toPosition().inside(e.killer.killer!!.location)
        }
        addSimpleCondition("name") { e ->
            asList().any { it.equals(e.mobType.internalName, true) }
        }
        addSimpleCondition("level") { e ->
            toDouble() == e.mobLevel
        }
        addSimpleCondition("min-level") { e ->
            toDouble() <= e.mobLevel
        }
        addConditionVariable("name") {
            it.mobType.internalName
        }
    }
}
