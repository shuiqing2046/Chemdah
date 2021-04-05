package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountable
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.inventory.ItemStack

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IItemRepair
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IItemRepair : ObjectiveCountable<PrepareAnvilEvent>() {

    override val name = "item repair"
    override val event = PrepareAnvilEvent::class

    val air = ItemStack(Material.AIR)
    val empty = Location(Bukkit.getWorlds()[0], 0.0, 0.0, 0.0)

    init {
        handler {
            viewers[0] as Player
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("position") || task.condition["position"]!!.toPosition().inside(e.inventory.location ?: empty)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("text") || task.condition["text"]!!.toString() in e.inventory.renameText.toString()
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("cost") || task.condition["cost"]!!.toInt() <= e.inventory.repairCost
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("item") || task.condition["item"]!!.toItem().match(e.inventory.result ?: air)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("item:matrix") || task.condition["item:matrix"]!!.toItem().run {
                match(e.inventory.firstItem ?: air) || match(e.inventory.secondItem ?: air)
            }
        }
    }
}