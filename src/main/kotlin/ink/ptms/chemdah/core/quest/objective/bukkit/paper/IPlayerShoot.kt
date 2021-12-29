package ink.ptms.chemdah.core.quest.objective.bukkit.paper

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.paper.IPlayerShoot
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerShoot : ObjectiveCountableI<PlayerLaunchProjectileEvent>() {

    override val name = "shoot projectile"
    override val event = PlayerLaunchProjectileEvent::class.java

    init {
        handler {
            player
        }
        addSimpleCondition("position") { e ->
            toPosition().inside(e.player.location)
        }
        addSimpleCondition("projectile") { e ->
            toInferEntity().isEntity(e.projectile)
        }
        addSimpleCondition("item") { e ->
            toInferItem().isItem(e.itemStack)
        }
        addSimpleCondition("consume") { e ->
            toBoolean() == e.shouldConsume()
        }
    }
}