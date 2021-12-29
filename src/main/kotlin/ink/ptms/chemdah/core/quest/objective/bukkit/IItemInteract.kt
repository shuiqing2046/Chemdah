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
    override val event = PlayerInteractEvent::class.java
    override val isAsync = true

    init {
        handler {
            if (it.action != Action.PHYSICAL && it.item.isNotAir()) it.player else null
        }
        addSimpleCondition("position") { data, e ->
            data.toPosition().inside(e.player.location)
        }
        addSimpleCondition("action") { data, e ->
            data.asList().any { it.equals(e.action.name, true) }
        }
        addSimpleCondition("hand") { data, e ->
            data.asList().any { it.equals(e.hand?.name, true) }
        }
        addSimpleCondition("item") { data, e ->
            data.toInferItem().isItem(e.item!!)
        }
    }
}