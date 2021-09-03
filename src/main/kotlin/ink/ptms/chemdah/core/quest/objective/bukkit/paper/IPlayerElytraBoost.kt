package ink.ptms.chemdah.core.quest.objective.bukkit.paper

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerElytraBoost
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerElytraBoost : ObjectiveCountableI<PlayerElytraBoostEvent>() {

    override val name = "elytra boost"
    override val event = PlayerElytraBoostEvent::class

    init {
        handler {
            player
        }
        addCondition("position") { e ->
            toPosition().inside(e.player.location)
        }
        addCondition("item") { e ->
            toInferItem().isItem(e.itemStack)
        }
        addCondition("consume") { e ->
            toBoolean() == e.shouldConsume()
        }
        addCondition("firework") { e ->
            toInferEntity().isEntity(e.firework)
        }
    }
}