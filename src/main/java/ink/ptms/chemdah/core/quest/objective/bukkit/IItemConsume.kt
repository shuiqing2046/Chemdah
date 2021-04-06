package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountable
import org.bukkit.event.player.PlayerItemConsumeEvent

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

    init {
        handler {
            player
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("position") || task.condition["position"]!!.toPosition().inside(e.player.location)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("item") || task.condition["item"]!!.toInferItem().isItem(e.item)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("item:replacement") || task.condition["item:replacement"]!!.toInferItem().isItem(e.replacement ?: AIR)
        }
    }
}