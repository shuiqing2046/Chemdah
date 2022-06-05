package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.ItemStack
import taboolib.common.reflect.Reflex.Companion.invokeMethod

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IItemConsume
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IItemConsume : ObjectiveCountableI<PlayerItemConsumeEvent>() {

    override val name = "item consume"
    override val event = PlayerItemConsumeEvent::class.java

    init {
        handler {
            it.player
        }
        addSimpleCondition("position") { data, e ->
            data.toPosition().inside(e.player.location)
        }
        addSimpleCondition("item") { data, e ->
            data.toInferItem().isItem(e.item)
        }
        addSimpleCondition("item:replacement") { data, e ->
            data.toInferItem().isItem(e.invokeMethod<ItemStack>("getReplacement") ?: EMPTY_ITEM)
        }
    }
}