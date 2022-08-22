package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.entity.Item
import org.bukkit.event.player.PlayerFishEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerFish
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerFish : ObjectiveCountableI<PlayerFishEvent>() {

    override val name = "player fish"
    override val event = PlayerFishEvent::class.java

    init {
        handler {
            it.player
        }
        addSimpleCondition("position") { data, e ->
            data.toPosition().inside(e.player.location)
        }
        addSimpleCondition("entity") { data, e ->
            data.toInferEntity().isEntity(e.caught)
        }
        addSimpleCondition("entity:hook") { data, e ->
            data.toInferEntity().isEntity(e.hook)
        }
        addSimpleCondition("item") { data, e ->
            data.toInferItem().isItem((e.caught as? Item)?.itemStack ?: return@addSimpleCondition false)
        }
        addSimpleCondition("state") { data, e ->
            data.asList().any { it.equals(e.state.name, true) }
        }
        addSimpleCondition("exp") { data, e ->
            data.toInt() <= e.expToDrop
        }
        addConditionVariable("exp") {
            it.expToDrop
        }
    }
}