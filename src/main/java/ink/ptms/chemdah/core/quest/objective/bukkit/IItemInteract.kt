package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import io.izzel.taboolib.util.item.Items
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IBlockInteract
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IItemInteract : ObjectiveCountableI<PlayerInteractEvent>() {

    override val name = "item interact"
    override val event = PlayerInteractEvent::class
    override val isAsync = true

    init {
        handler {
            if (action != Action.PHYSICAL && Items.nonNull(item)) player else null
        }
        addCondition("position") { e ->
            toPosition().inside(e.player.location)
        }
        addCondition("action") { e ->
            asList().any { it.equals(e.action.name, true) }
        }
        addCondition("hand") { e ->
            asList().any { it.equals(e.hand?.name, true) }
        }
        addCondition("item") { e ->
            toInferItem().isItem(e.item!!)
        }
    }
}