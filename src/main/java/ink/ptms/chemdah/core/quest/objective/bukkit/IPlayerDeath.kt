package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import io.izzel.taboolib.util.lite.Servers
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerDeath
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerDeath : AEntityDeath<PlayerDeathEvent>() {

    override val name = "player death"
    override val event = PlayerDeathEvent::class

    init {
        handler {
            entity
        }
        addCondition("weapon") { e ->
            val el = e.entity.lastDamageCause as? EntityDamageByEntityEvent ?: return@addCondition false
            toInferItem().isItem(Servers.getLivingAttackerInDamageEvent(el)?.equipment?.itemInMainHand ?: AIR)
        }
        addCondition("attacker") { e ->
            val el = e.entity.lastDamageCause as? EntityDamageByEntityEvent ?: return@addCondition false
            toInferEntity().isEntity(Servers.getLivingAttackerInDamageEvent(el) ?: return@addCondition false)
        }
        addCondition("message") { e ->
            toString() in e.deathMessage.toString()
        }
        addConditionVariable("message") { e ->
            e.deathMessage.toString()
        }
    }
}