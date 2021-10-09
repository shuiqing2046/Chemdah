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
        addCondition("name") {
            toString() == it.entity.name
        }
        addCondition("level") {
            toDouble() == it.mobLevel
        }
        addCondition("min-level") {
            toDouble() <= it.mobLevel
        }
    }
}