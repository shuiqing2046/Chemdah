package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import taboolib.platform.util.attacker

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
    override val event = EntityDamageByEntityEvent::class.java

    init {
        handler {
            attacker as? Player
        }
        addSimpleCondition("attacker") { e ->
            toInferEntity().isEntity(e.attacker)
        }
        addSimpleCondition("weapon") { e ->
            toInferItem().isItem(e.attacker!!.equipment?.itemInMainHand ?: AIR)
        }
    }
}