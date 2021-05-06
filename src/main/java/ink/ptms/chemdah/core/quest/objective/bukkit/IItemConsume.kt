package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.event.player.PlayerItemConsumeEvent

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
    override val event = PlayerItemConsumeEvent::class

    init {
        handler {
            player
        }
        addCondition("position") { e ->
            toPosition().inside(e.player.location)
        }
        addCondition("item") { e ->
            toInferItem().isItem(e.item)
        }
        addCondition("item:replacement") { e ->
            toInferItem().isItem(e.replacement ?: AIR)
        }
    }
}