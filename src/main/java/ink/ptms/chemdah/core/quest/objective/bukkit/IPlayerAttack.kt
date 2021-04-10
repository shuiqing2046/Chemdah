package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import io.izzel.taboolib.util.lite.Servers
import org.bukkit.event.entity.EntityDamageByEntityEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerAttack
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerAttack : AEntityDamage<EntityDamageByEntityEvent>() {

    override val name = "player attack"
    override val event = EntityDamageByEntityEvent::class

    init {
        handler {
            Servers.getAttackerInDamageEvent(this)
        }
        addCondition("weapon") { e ->
            toInferItem().isItem(Servers.getAttackerInDamageEvent(e).inventory.itemInMainHand)
        }
    }
}