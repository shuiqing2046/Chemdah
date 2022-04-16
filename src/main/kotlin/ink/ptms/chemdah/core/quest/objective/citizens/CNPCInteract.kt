package ink.ptms.chemdah.core.quest.objective.citizens

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import net.citizensnpcs.api.event.NPCRightClickEvent

@Dependency("Citizens")
object CNPCInteract : ObjectiveCountableI<NPCRightClickEvent>() {

    override val name = "cnpc interact"
    override val event = NPCRightClickEvent::class.java

    init {
        handler {
            it.clicker
        }
        addSimpleCondition("position") { data, e ->
            data.toPosition().inside(e.npc.entity.location)
        }
        addSimpleCondition("id") { data, e ->
            data.toInt() == e.npc.id
        }
        addSimpleCondition("name") { data, e ->
            data.asList().any { it.equals(e.npc.name, true)}
        }
        addSimpleCondition("type") { data, e ->
            data.asList().any { it.equals(e.npc.entity.type.name, true) }
        }
        addConditionVariable("id") {
            it.npc.id
        }
        addConditionVariable("name") {
            it.npc.name
        }
    }
}