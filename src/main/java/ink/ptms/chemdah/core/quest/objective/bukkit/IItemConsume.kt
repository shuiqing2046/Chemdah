package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountable
import org.bukkit.Material
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.ItemStack

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IItemConsume
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IItemConsume : ObjectiveCountable<PlayerItemConsumeEvent>() {

    override val name = "item consume"
    override val event = PlayerItemConsumeEvent::class

    val air = ItemStack(Material.AIR)

    init {
        handler {
            player
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("position") || task.condition["position"]!!.toPosition().inside(e.player.location)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("item") || task.condition["item"]!!.toItem().match(e.item)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("item:replacement") || task.condition["item:replacement"]!!.toItem().match(e.replacement ?: air)
        }
    }
}