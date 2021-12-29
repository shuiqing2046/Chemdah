package ink.ptms.chemdah.core.quest.objective.citizens

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import net.citizensnpcs.api.event.NPCDamageByEntityEvent
import org.bukkit.entity.Player

@Dependency("Citizens")
object CNPCDamage : ObjectiveCountableI<NPCDamageByEntityEvent>() {

    override val name = "cnpc damage"
    override val event = NPCDamageByEntityEvent::class.java

    init {
        handler {
            damager as? Player
        }
        addSimpleCondition("position") { e ->
            toPosition().inside(e.damager.location)
        }
        addSimpleCondition("name") { e ->
            asList().any { it.equals(e.npc.name, true) }
        }
        addSimpleCondition("id") { e ->
            toInt() == e.npc.id
        }
        addSimpleCondition("type") { e ->
            asList().any { it.equals(e.npc.entity.type.name, true) }
        }
        addSimpleCondition("damage") {
            toInt() <= it.damage
        }
        addConditionVariable("name") {
            it.npc.name
        }
        addConditionVariable("damage") {
            it.damage
        }
        addConditionVariable("id") {
            it.npc.id
        }
    }
}
