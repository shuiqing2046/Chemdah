package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountable
import org.bukkit.event.player.PlayerInteractEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IBlockInteract
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IBlockInteract : ObjectiveCountable<PlayerInteractEvent>() {

    override val name = "block interact"
    override val event = PlayerInteractEvent::class

    init {
        handler {
            if (clickedBlock != null) player else null
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("position") || task.condition["position"]!!.toPosition().inside(e.clickedBlock!!.location)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("material") || task.condition["material"]!!.toMaterial().isBlock(e.clickedBlock!!)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("action") || task.condition["action"]!!.asList().any { it.equals(e.action.name, true) }
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("face") || task.condition["face"]!!.asList().any { it.equals(e.blockFace.name, true) }
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("hand") || task.condition["hand"]!!.asList().any { it.equals(e.hand?.name, true) }
        }
    }
}