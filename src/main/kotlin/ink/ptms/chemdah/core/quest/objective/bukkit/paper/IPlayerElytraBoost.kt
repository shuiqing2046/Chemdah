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
    override val event = PlayerElytraBoostEvent::class.java

    init {
        handler {
            it.player
        }
        addSimpleCondition("position") { data, e ->
            data.toPosition().inside(e.player.location)
        }
        addSimpleCondition("item") { data, e ->
            data.toInferItem().isItem(e.itemStack)
        }
        addSimpleCondition("consume") { data, e ->
            data.toBoolean() == e.shouldConsume()
        }
        addSimpleCondition("firework") { data, e ->
            data.toInferEntity().isEntity(e.firework)
        }
    }
}