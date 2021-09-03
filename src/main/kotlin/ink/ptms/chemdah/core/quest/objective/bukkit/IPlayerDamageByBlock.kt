package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByBlockEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerDamageByBlock
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerDamageByBlock : AEntityDamage<EntityDamageByBlockEvent>() {

    override val name = "player damage by block"
    override val event = EntityDamageByBlockEvent::class

    init {
        handler {
            if (damager != null) entity as? Player else null
        }
        addCondition("block") { e ->
            toInferBlock().isBlock(e.damager!!)
        }
    }
}