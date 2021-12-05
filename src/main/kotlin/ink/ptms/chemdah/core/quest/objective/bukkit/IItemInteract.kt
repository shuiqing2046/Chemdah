package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import taboolib.platform.util.isNotAir

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
            if (action != Action.PHYSICAL && item.isNotAir()) player else null
        }
        addSimpleCondition("position") { e ->
            toPosition().inside(e.player.location)
        }
        addSimpleCondition("action") { e ->
            asList().any { it.equals(e.action.name, true) }
        }
        addSimpleCondition("hand") { e ->
            asList().any { it.equals(e.hand?.name, true) }
        }
        addSimpleCondition("item") { e ->
            toInferItem().isItem(e.item!!)
        }
    }
}