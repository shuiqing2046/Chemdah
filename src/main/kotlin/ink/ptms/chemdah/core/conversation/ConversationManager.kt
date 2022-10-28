package ink.ptms.chemdah.core.conversation

import com.sucy.skill.api.event.PlayerCastSkillEvent
import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.api.ChemdahAPI.conversationSession
import ink.ptms.chemdah.api.event.collect.ConversationEvents
import ink.ptms.chemdah.api.event.collect.PlayerEvents
import ink.ptms.chemdah.util.hidden
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Schedule
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.OptionalEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import java.util.concurrent.ConcurrentHashMap

/**
 * Chemdah
 * ink.ptms.chemdah.core.conversation.ConversationManager
 *
 * @author sky
 * @since 2021/2/9 8:46 下午
 */
object ConversationManager {

    private val effects = ConcurrentHashMap<String, List<PotionEffect>>()
    private val effectFreeze = mapOf(PotionEffectType.BLINDNESS to 0, PotionEffectType.SLOW to 2)

    @Config("core/conversation.yml", migrate = true)
    lateinit var conf: Configuration
        private set

    val sessions = ConcurrentHashMap<String, Session>()

    fun getConversation(player: Player, namespace: String, source: Any?, vararg name: String): Conversation? {
        val conversation = ChemdahAPI.conversation.values.firstOrNull { name.any { name -> it.isNPC(namespace, name) } }
        val event = ConversationEvents.Select(player, namespace, name.toList(), conversation, source)
        return if (event.call()) event.conversation else null
    }

    @Schedule(period = 1, async = true)
    private fun onTick() {
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
                submit { session.close(refuse = true) }
            }
        }
    }

    @Awake(LifeCycle.DISABLE)
    private fun onDisable() {
        Bukkit.getOnlinePlayers().forEach { player ->
            if (player.conversationSession?.conversation?.hasFlag("NO_EFFECT") == false) {
                effectFreeze.forEach { player.removePotionEffect(it.key) }
                effects.remove(player.name)?.forEach { player.addPotionEffect(it) }
            }
            player.conversationSession?.close(refuse = true)
        }
    }

    @SubscribeEvent
    private fun onReleased(e: PlayerEvents.Released) {
        val player = e.player
        if (player.conversationSession?.conversation?.hasFlag("NO_EFFECT") == false) {
            effectFreeze.forEach { player.removePotionEffect(it.key) }
            effects.remove(player.name)?.forEach { e.player.addPotionEffect(it) }
        }
        player.conversationSession?.close(refuse = true)
        sessions.remove(player.name)
    }

    @Suppress("UNCHECKED_CAST")
    @SubscribeEvent
    private fun onBegin(e: ConversationEvents.Begin) {
        if (!e.conversation.hasFlag("NO_EFFECT")) {
            effects[e.session.player.name] = effectFreeze.mapNotNull { e.session.player.getPotionEffect(it.key) }.filter { it.duration in 10..9999 }
            effectFreeze.forEach {
                // 取消特定效果
                if (e.conversation.hasFlag("NO_EFFECT:${it.key.name.uppercase()}")) {
                    return@forEach
                }
                e.session.player.addPotionEffect(PotionEffect(it.key, 99999, it.value).hidden())
            }
        }
        if (e.conversation.hasFlag("FORCE_LOOK")) {
            val source = e.session.source as Source<Any>
            val direction = source.getOriginLocation(source.entity).subtract(e.session.player.eyeLocation).toVector().normalize()
            val temp = e.session.player.location.clone()
            temp.direction = direction
            e.session.player.teleport(temp)
        }
    }

    @SubscribeEvent
    private fun onClosed(e: ConversationEvents.Closed) {
        if (!e.session.conversation.hasFlag("NO_EFFECT")) {
            effectFreeze.forEach { e.session.player.removePotionEffect(it.key) }
            effects.remove(e.session.player.name)?.forEach { e.session.player.addPotionEffect(it) }
            // 视觉效果
            if (!e.session.player.hasPotionEffect(PotionEffectType.BLINDNESS)) {
                e.session.player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 20, 0))
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onDamage(e: EntityDamageEvent) {
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
            submit { session.close(refuse = true) }
        }
    }

    @SubscribeEvent(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onMove(e: PlayerMoveEvent) {
        if (e.player.conversationSession?.conversation?.hasFlag("NO_MOVE") == true) {
            e.setTo(e.from)
        }
    }

    @SubscribeEvent(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onDropItem(e: PlayerDropItemEvent) {
        if (e.player.conversationSession?.conversation?.hasFlag("FORCE_DISPLAY") == true) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent
    private fun onCommand(e: PlayerCommandPreprocessEvent) {
        if (e.message.startsWith("/session")) {
            e.isCancelled = true
            val args = e.message.split(" ").toMutableList().also { it.removeFirst() }
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

    @SubscribeEvent(bind = "com.sucy.skill.api.event.PlayerCastSkillEvent")
    private fun onPlayerCastSkillEvent(oe: OptionalEvent) {
        val e = oe.get<PlayerCastSkillEvent>()
        if (e.player.conversationSession != null) {
            e.isCancelled = true
        }
    }
}