package ink.ptms.chemdah.core.quest.objective.citizens

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import net.citizensnpcs.api.event.NPCDamageByEntityEvent
import org.bukkit.entity.Player

@Dependency("Citizens")
object CNPCDamage : ObjectiveCountableI<NPCDamageByEntityEvent>() {

    override val name = "cnpc damage"
    override val event = NPCDamageByEntityEvent::class

    init {
        handler {
            damager as? Player
        }
        addCondition("position") { e ->
            toPosition().inside(e.damager.location)
        }
        addCondition("name") { e ->
            asList().any { it.equals(e.npc.name, true) }
        }
        addCondition("type") { e ->
            asList().any { it.equals(e.npc.entity.type.name, true) }
        }
        addCondition("damage") {
            toInt() <= it.damage
        }
        addConditionVariable("name") {
            it.npc.name
        }
        addConditionVariable("damage") {
            it.damage
        }
    }
}