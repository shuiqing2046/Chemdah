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
            viewers[0] as Player
        }
        addSimpleCondition("position") { e ->
            toPosition().inside(e.inventory.location ?: EMPTY)
        }
        addSimpleCondition("text") { e ->
            toString() in e.inventory.renameText.toString()
        }
        addSimpleCondition("cost") { e ->
            toInt() <= e.inventory.repairCost
        }
        addSimpleCondition("item") { e ->
            toInferItem().isItem(e.inventory.getItem(2) ?: AIR)
        }
        addSimpleCondition("item:matrix") { e ->
            toInferItem().run {
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