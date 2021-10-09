package ink.ptms.chemdah.core.quest.objective.mythicmobs

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent

@Dependency("MythicMobs")
object MMythicKillType : ObjectiveCountableI<MythicMobDeathEvent>() {

    override val name = "mythicmobs kill"
    override val event = MythicMobDeathEvent::class

    init {
        handler {
            killer.killer
        }
        addCondition("position") { e ->
            toPosition().inside(e.killer.killer!!.location)
        }
        addCondition("name") { e ->
            asList().any { it.equals(e.mobType.internalName, true) }
        }
        addCondition("level") { e ->
            toDouble() == e.mobLevel
        }
        addCondition("min-level") { e ->
            toDouble() <= e.mobLevel
        }
        addConditionVariable("name") {
            it.mobType.internalName
        }
    }
}