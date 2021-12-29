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
            it.damager as? Player
        }
        addSimpleCondition("position") { data, e ->
            data.toPosition().inside(e.damager.location)
        }
        addSimpleCondition("name") { data, e ->
            data.asList().any { it.equals(e.npc.name, true) }
        }
        addSimpleCondition("id") { data, e ->
            data.toInt() == e.npc.id
        }
        addSimpleCondition("type") { data, e ->
            data.asList().any { it.equals(e.npc.entity.type.name, true) }
        }
        addSimpleCondition("damage") { data, it ->
            data.toInt() <= it.damage
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
