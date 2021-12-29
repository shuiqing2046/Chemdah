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
            clicker
        }
        addSimpleCondition("position") { e ->
            toPosition().inside(e.npc.entity.location)
        }
        addSimpleCondition("id") { e ->
            toInt() == e.npc.id
        }
        addSimpleCondition("name") { e ->
            asList().any { it.equals(e.npc.name, true)}
        }
        addSimpleCondition("type") { e ->
            asList().any { it.equals(e.npc.entity.type.name, true) }
        }
        addConditionVariable("id") {
            it.npc.id
        }
        addConditionVariable("name") {
            it.npc.name
        }
    }
}
