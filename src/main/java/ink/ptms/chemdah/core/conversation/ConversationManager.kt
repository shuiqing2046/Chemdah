package ink.ptms.chemdah.core.conversation

import ink.ptms.adyeshach.api.event.AdyeshachEntityInteractEvent
import ink.ptms.adyeshach.common.entity.type.AdyHuman
import ink.ptms.chemdah.Chemdah
import ink.ptms.chemdah.api.ChemdahAPI
import io.izzel.taboolib.kotlin.Tasks
import io.izzel.taboolib.module.config.TConfig
import io.izzel.taboolib.module.i18n.I18n
import io.izzel.taboolib.module.inject.PlayerContainer
import io.izzel.taboolib.module.inject.TInject
import io.izzel.taboolib.module.inject.TListener
import io.izzel.taboolib.module.inject.TSchedule
import io.lumine.xikage.mythicmobs.MythicMobs
import net.citizensnpcs.api.CitizensAPI
import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.inventory.EquipmentSlot
import java.util.concurrent.ConcurrentHashMap

/**
 * Chemdah
 * ink.ptms.chemdah.core.conversation.Compat
 *
 * @author sky
 * @since 2021/2/9 8:46 下午
 */
@TListener
object ConversationManager : Listener {

    @TInject("conversation.yml")
    lateinit var conf: TConfig
        private set

    @PlayerContainer
    val sessions = ConcurrentHashMap<String, Session>()

    @TSchedule(period = 1, async = true)
    private fun tick() {
        Bukkit.getOnlinePlayers().forEach {
            val session = sessions[it.name] ?: return@forEach
            if (session.isClosed) {
                return@forEach
            }
            // 远离或背对对话单位
            if (session.location.world!!.name != session.player.world.name
                || session.location.direction.dot(session.player.location.direction) < 0
                || session.distance > 0.5
            ) {
                session.isClosed = true
                Tasks.task {
                    session.close(refuse = true)
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun e(e: EntityDamageEvent) {
        if (e.entity is Player) {
            val session = sessions[e.entity.name] ?: return
            if (!session.isClosed) {
                session.isClosed = true
                Tasks.task {
                    session.close(refuse = true)
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun e(e: PlayerInteractAtEntityEvent) {
        if (e.hand == EquipmentSlot.HAND && ChemdahAPI.getConversationSession(e.player) == null) {
            val name = I18n.get().getName(e.rightClicked)
            ChemdahAPI.conversation.values.firstOrNull { it.isNPC("minecraft", name) }?.run {
                e.isCancelled = true
                open(e.player, e.rightClicked.location.also {
                    it.y += e.rightClicked.height
                })
                ChemdahAPI.getConversationSession(e.player)?.npcName = name
            }
        }
    }

    @TListener(depend = ["Adyeshach"])
    private class CompatAdyeshach : Listener {

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        fun e(e: AdyeshachEntityInteractEvent) {
            if (e.isMainHand && ChemdahAPI.getConversationSession(e.player) == null) {
                ChemdahAPI.conversation.values.firstOrNull { it.isNPC("adyeshach", e.entity.id) }?.run {
                    e.isCancelled = true
                    Tasks.task {
                        open(e.player, e.entity.getLocation().also {
                            it.y += e.entity.entityType.entitySize.height
                        })
                        ChemdahAPI.getConversationSession(e.player)?.npcName = if (e.entity is AdyHuman) {
                            (e.entity as AdyHuman).getName()
                        } else {
                            e.entity.getCustomName()
                        }
                    }
                }
            }
        }
    }

    @TListener(depend = ["Citizens"])
    private class CompatCitizens : Listener {

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        fun e(e: PlayerInteractAtEntityEvent) {
            if (e.hand == EquipmentSlot.HAND && e.rightClicked.hasMetadata("NPC") && ChemdahAPI.getConversationSession(e.player) == null) {
                val npc = CitizensAPI.getNPCRegistry().getNPC(e.rightClicked) ?: return
                ChemdahAPI.conversation.values.firstOrNull { it.isNPC("citizens", npc.id.toString()) }?.run {
                    e.isCancelled = true
                    open(e.player, e.rightClicked.location.also {
                        it.y += e.rightClicked.height
                    })
                    ChemdahAPI.getConversationSession(e.player)?.npcName = npc.fullName
                }
            }
        }
    }

    @TListener(depend = ["MythicMobs"])
    private class CompatMythicMobs : Listener {

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        fun e(e: PlayerInteractAtEntityEvent) {
            if (e.hand == EquipmentSlot.HAND && e.rightClicked is LivingEntity && ChemdahAPI.getConversationSession(e.player) == null) {
                val mob = MythicMobs.inst().mobManager.getMythicMobInstance(e.rightClicked) ?: return
                ChemdahAPI.conversation.values.firstOrNull { it.isNPC("citizens", mob.type.internalName) }?.run {
                    e.isCancelled = true
                    open(e.player, e.rightClicked.location.also {
                        it.y += e.rightClicked.height
                    })
                    ChemdahAPI.getConversationSession(e.player)?.npcName = mob.displayName
                }
            }
        }
    }
}