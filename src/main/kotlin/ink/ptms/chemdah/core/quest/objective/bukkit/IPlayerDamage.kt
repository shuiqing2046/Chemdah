package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerDamage
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerDamage : AEntityDamage<EntityDamageEvent>() {

    override val name = "player damage"
    override val event = EntityDamageEvent::class.java

    init {
        handler {
            it.entity as? Player
        }
    }
}