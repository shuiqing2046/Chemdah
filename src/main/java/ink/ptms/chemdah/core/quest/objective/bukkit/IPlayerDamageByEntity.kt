package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import io.izzel.taboolib.util.lite.Servers
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerDamageByEntity
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerDamageByEntity : AEntityDamage<EntityDamageByEntityEvent>() {

    override val name = "player damage by entity"
    override val event = EntityDamageByEntityEvent::class

    init {
        handler {
            if (Servers.getLivingAttackerInDamageEvent(this) != null) entity as? Player else null
        }
        addCondition("attacker") { e ->
            toInferEntity().isEntity(Servers.getLivingAttackerInDamageEvent(e))
        }
        addCondition("weapon") { e ->
            toInferItem().isItem(Servers.getLivingAttackerInDamageEvent(e).equipment?.itemInMainHand ?: AIR)
        }
    }
}