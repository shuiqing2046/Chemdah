package ink.ptms.chemdah.core.quest.objective.citizens

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import net.citizensnpcs.api.event.NPCRightClickEvent

@Dependency("Citizens")
object CNPCInteract : ObjectiveCountableI<NPCRightClickEvent>() {

    override val name = "cnpc interact"
    override val event = NPCRightClickEvent::class

    init {
        handler {
            clicker
        }
        addCondition("position") { e ->
            toPosition().inside(e.npc.entity.location)
        }
        addCondition("id") { e ->
            toInt() == e.npc.id
        }
        addCondition("name") { e ->
            asList().any { it.equals(e.npc.name, true)}
        }
        addCondition("type") { e ->
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
