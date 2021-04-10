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
        addCondition("position") { e ->
            toPosition().inside(e.inventory.location ?: EMPTY)
        }
        addCondition("text") { e ->
            toString() in e.inventory.renameText.toString()
        }
        addCondition("cost") { e ->
            toInt() <= e.inventory.repairCost
        }
        addCondition("item") { e ->
            toInferItem().isItem(e.inventory.result ?: AIR)
        }
        addCondition("item:matrix") { e ->
            toInferItem().run {
                isItem(e.inventory.firstItem ?: AIR) || isItem(e.inventory.secondItem ?: AIR)
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