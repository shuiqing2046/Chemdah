package ink.ptms.chemdah.core.conversation

import com.sucy.skill.api.event.PlayerCastSkillEvent
import ink.ptms.adyeshach.api.event.AdyeshachEntityInteractEvent
import ink.ptms.adyeshach.common.entity.EntityInstance
import ink.ptms.adyeshach.common.entity.type.AdyHuman
import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.api.ChemdahAPI.conversationSession
import ink.ptms.chemdah.api.event.collect.ConversationEvents
import ink.ptms.chemdah.util.hidden
import io.izzel.taboolib.kotlin.Tasks
import io.izzel.taboolib.module.ai.SimpleAiSelector
import io.izzel.taboolib.module.config.TConfig
import io.izzel.taboolib.module.i18n.I18n
import io.izzel.taboolib.module.inject.*
import io.lumine.xikage.mythicmobs.MythicMobs
import io.lumine.xikage.mythicmobs.mobs.ActiveMob
import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.npc.NPC
import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.*
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
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

    private val effects = ConcurrentHashMap<String, List<PotionEffect>>()
    private val effectFreeze = setOf(PotionEffectType.BLINDNESS to 0, PotionEffectType.SLOW to 4)

    @TInject("core/conversation.yml", migrate = true)
    lateinit var conf: TConfig
        private set

    @PlayerContainer
    val sessions = ConcurrentHashMap<String, Session>()

    fun getConversation(namespace: String, name: String) = ChemdahAPI.conversation.values.firstOrNull { it.isNPC(namespace, name) }

    @TSchedule(period = 1, async = true)
    private fun tick() {
        Bukkit.getOnlinePlayers().forEach { p ->
            val session = p.conversationSession ?: return@forEach
            if (session.isClosed || session.conversation.hasFlag("FORCE_DISPLAY")) {
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

    @TFunction.Cancel
    private fun cancel() {
        Bukkit.getOnlinePlayers().forEach { p ->
            if (p.conversationSession?.conversation?.hasFlag("NO_EFFECT") == false) {
                effectFreeze.forEach { p.removePotionEffect(it.first) }
                effects.remove(p.name)?.forEach { p.addPotionEffect(it) }
            }
            p.conversationSession?.close(refuse = true)
        }
    }

    @EventHandler
    private fun e(e: PlayerQuitEvent) {
        if (e.player.conversationSession?.conversation?.hasFlag("NO_EFFECT") == false) {
            effectFreeze.forEach { e.player.removePotionEffect(it.first) }
            effects.remove(e.player.name)?.forEach { e.player.addPotionEffect(it) }
        }
        e.player.conversationSession?.close(refuse = true)
    }

    @EventHandler
    private fun e(e: ConversationEvents.Begin) {
        if (!e.conversation.hasFlag("NO_EFFECT")) {
            effects[e.session.player.name] = effectFreeze.mapNotNull { e.session.player.getPotionEffect(it.first) }.filter { it.duration in 10..9999 }
            effectFreeze.forEach { e.session.player.addPotionEffect(PotionEffect(it.first, 99999, it.second).hidden()) }
        }
    }

    @EventHandler
    private fun e(e: ConversationEvents.Closed) {
        if (!e.session.conversation.hasFlag("NO_EFFECT")) {
            effectFreeze.forEach { e.session.player.removePotionEffect(it.first) }
            effects.remove(e.session.player.name)?.forEach { e.session.player.addPotionEffect(it) }
        }
        // 视觉效果
        if (!e.session.player.hasPotionEffect(PotionEffectType.BLINDNESS)) {
            e.session.player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 20, 0))
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun e(e: EntityDamageEvent) {
        if (e.entity is Player) {
            val session = sessions[e.entity.name] ?: return
            if (session.isClosed) {
                return
            }
            if (session.conversation.hasFlag("FORCE_DISPLAY")) {
                e.isCancelled = true
                return
            }
            session.isClosed = true
            Tasks.task {
                session.close(refuse = true)
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun e(e: PlayerMoveEvent) {
        if (e.player.conversationSession?.conversation?.hasFlag("NO_MOVE") == true && (e.from.x != e.to.x || e.from.z != e.to.z)) {
            e.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun e(e: PlayerDropItemEvent) {
        if (e.player.conversationSession?.conversation?.hasFlag("FORCE_DISPLAY") == true) {
            e.isCancelled = true
        }
    }

    @EventHandler
    private fun e(e: PlayerCommandPreprocessEvent) {
        if (e.message.startsWith("/session")) {
            e.isCancelled = true
            val args = e.message.split(" ").toMutableList().also {
                it.removeFirst()
            }
            if (args.size == 2 && args[0] == "reply") {
                val session = e.player.conversationSession ?: return
                val reply = session.conversation.playerSide.reply.firstOrNull { it.uuid.toString() == args[1] } ?: return
                reply.check(session).thenApply { cond ->
                    if (cond) {
                        reply.select(session)
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun e(e: PlayerInteractAtEntityEvent) {
        if (e.hand == EquipmentSlot.HAND && e.player.conversationSession == null) {
            val name = I18n.get().getName(e.rightClicked)
            val conversation = getConversation("minecraft", name) ?: return
            val origin = e.rightClicked.location.add(0.0, e.rightClicked.height, 0.0)
            conversation.open(e.player, origin, npcName = name, npcObject = e.rightClicked)
            e.isCancelled = true
        }
    }

    @TListener(depend = ["Adyeshach"])
    private class CompatAdyeshach : Listener {

        @EventHandler
        fun e(e: ConversationEvents.Begin) {
            val npc = e.session.npcObject
            if (npc is EntityInstance) {
                npc.setTag("isFreeze", "true")
                npc.setTag("conversation:${e.session.player.name}", "conversation")
                // 让 NPC 看向玩家
                if (e.conversation.hasFlag("LOOK_PLAYER")) {
                    npc.controllerLook(e.session.player.eyeLocation, smooth = true)
                }
            }
        }

        @EventHandler
        fun e(e: ConversationEvents.Closed) {
            val npc = e.session.npcObject
            if (npc is EntityInstance) {
                npc.removeTag("conversation:${e.session.player.name}")
                // 若没有玩家在与该 NPC 对话
                if (npc.getTags().none { it.value == "conversation" }) {
                    npc.removeTag("isFreeze")
                }
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        fun e(e: AdyeshachEntityInteractEvent) {
            if (e.isMainHand && e.player.conversationSession == null) {
                getConversation("adyeshach", e.entity.id)?.run {
                    e.isCancelled = true
                    Tasks.task {
                        open(
                            e.player, e.entity.getLocation().also {
                                it.y += e.entity.entityType.entitySize.height
                            }, npcName = if (e.entity is AdyHuman) {
                                (e.entity as AdyHuman).getName()
                            } else {
                                e.entity.getCustomName()
                            }, npcObject = e.entity
                        )
                    }
                }
            }
        }
    }

    @TListener(depend = ["Citizens"])
    private class CompatCitizens : Listener {

        @EventHandler
        fun e(e: ConversationEvents.Begin) {
            val npc = e.session.npcObject
            if (npc is NPC && e.conversation.hasFlag("LOOK_PLAYER")) {
                npc.faceLocation(e.session.player.eyeLocation)
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        fun e(e: PlayerInteractAtEntityEvent) {
            if (e.hand == EquipmentSlot.HAND && e.rightClicked.hasMetadata("NPC") && e.player.conversationSession == null) {
                val npc = CitizensAPI.getNPCRegistry().getNPC(e.rightClicked) ?: return
                getConversation("citizens", npc.id.toString())?.run {
                    e.isCancelled = true
                    open(e.player, e.rightClicked.location.also {
                        it.y += e.rightClicked.height
                    }, npcName = npc.fullName, npcObject = npc)
                }
            }
        }
    }

    @TListener(depend = ["MythicMobs"])
    private class CompatMythicMobs : Listener {

        @EventHandler
        fun e(e: ConversationEvents.Begin) {
            val npc = e.session.npcObject
            if (npc is ActiveMob && npc.entity.bukkitEntity is LivingEntity && e.conversation.hasFlag("LOOK_PLAYER")) {
                SimpleAiSelector.getExecutor().controllerLookAt(npc.entity.bukkitEntity as LivingEntity, e.session.player)
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        fun e(e: PlayerInteractAtEntityEvent) {
            if (e.hand == EquipmentSlot.HAND && e.rightClicked is LivingEntity && e.player.conversationSession == null) {
                val mob = MythicMobs.inst().mobManager.getMythicMobInstance(e.rightClicked) ?: return
                getConversation("mythicmobs", mob.type.internalName)?.run {
                    e.isCancelled = true
                    open(e.player, e.rightClicked.location.also {
                        it.y += e.rightClicked.height
                    }, npcName = mob.displayName, npcObject = mob)
                }
            }
        }
    }

    @TListener(depend = ["SkillAPI"])
    private class CompatSkillAPI : Listener {

        @EventHandler
        fun e(e: PlayerCastSkillEvent) {
            if (e.player.conversationSession != null) {
                e.isCancelled = true
            }
        }
    }
}