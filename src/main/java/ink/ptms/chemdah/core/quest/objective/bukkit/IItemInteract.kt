package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountable
import org.bukkit.Material
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IBlockInteract
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IItemInteract : ObjectiveCountable<PlayerInteractEvent>() {

    override val name = "item interact"
    override val event = PlayerInteractEvent::class

    val air = ItemStack(Material.AIR)

    init {
        handler {
            if (action != Action.PHYSICAL) player else null
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("position") || task.condition["position"]!!.toPosition().inside(e.player.location)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("action") || task.condition["action"]!!.asList().any { it.equals(e.action.name, true) }
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("item") || task.condition["item"]!!.toItem().match(e.item ?: air)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("hand") || task.condition["hand"]!!.asList().any { it.equals(e.hand?.name, true) }
        }
    }
}