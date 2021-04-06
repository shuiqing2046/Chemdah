package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountable
import org.bukkit.entity.Player
import org.bukkit.event.inventory.PrepareAnvilEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IItemAnvil
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IItemAnvil : ObjectiveCountable<PrepareAnvilEvent>() {

    override val name = "player anvil"
    override val event = PrepareAnvilEvent::class

    init {
        handler {
            viewers[0] as Player
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("position") || task.condition["position"]!!.toPosition().inside(e.inventory.location ?: EMPTY)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("text") || task.condition["text"]!!.toString() in e.inventory.renameText.toString()
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("cost") || task.condition["cost"]!!.toInt() <= e.inventory.repairCost
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("item") || task.condition["item"]!!.toInferItem().isItem(e.inventory.result ?: AIR)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("item:matrix") || task.condition["item:matrix"]!!.toInferItem().run {
                isItem(e.inventory.firstItem ?: AIR) || isItem(e.inventory.secondItem ?: AIR)
            }
        }
    }
}