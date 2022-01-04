package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
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
object IItemAnvil : ObjectiveCountableI<PrepareAnvilEvent>() {

    override val name = "player anvil"
    override val event = PrepareAnvilEvent::class.java

    init {
        handler {
            it.viewers[0] as Player
        }
        addSimpleCondition("position") { data, e ->
            data.toPosition().inside(e.inventory.location ?: EMPTY)
        }
        addSimpleCondition("text") { data, e ->
            data.toString() in e.inventory.renameText.toString()
        }
        addSimpleCondition("cost") { data, e ->
            data.toInt() <= e.inventory.repairCost
        }
        addSimpleCondition("item") { data, e ->
            data.toInferItem().isItem(e.inventory.getItem(2) ?: AIR)
        }
        addSimpleCondition("item:matrix") { data, e ->
            data.toInferItem().run {
                isItem(e.inventory.getItem(0) ?: AIR) || isItem(e.inventory.getItem(1) ?: AIR)
            }
        }
        addConditionVariable("text") {
            it.inventory.renameText.toString()
        }
        addConditionVariable("cost") {
            it.inventory.repairCost
        }
    }
}