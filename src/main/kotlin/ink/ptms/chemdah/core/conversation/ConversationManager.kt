package ink.ptms.chemdah.core.conversation

import com.sucy.skill.api.event.PlayerCastSkillEvent
import ink.ptms.adyeshach.api.AdyeshachAPI
import ink.ptms.adyeshach.api.event.AdyeshachEntityInteractEvent
import ink.ptms.adyeshach.common.entity.EntityInstance
import ink.ptms.adyeshach.common.entity.ai.expand.ControllerLookAtPlayerAlways
import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.api.ChemdahAPI.conversationSession
import ink.ptms.chemdah.api.event.collect.ConversationEvents
import ink.ptms.chemdah.api.event.collect.PlayerEvents
import ink.ptms.chemdah.util.hidden
import io.lumine.xikage.mythicmobs.MythicMobs
import io.lumine.xikage.mythicmobs.mobs.ActiveMob
import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.npc.NPC
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Schedule
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.OptionalEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.common5.Baffle
import taboolib.common5.Coerce
import taboolib.module.ai.controllerLookAt
import taboolib.module.configuration.Config
import taboolib.module.configuration.SecuredFile
import taboolib.module.nms.getI18nName
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Chemdah
 * ink.ptms.chemdah.core.conversation.Compat
 *
 * @author sky
 * @since 2021/2/9 8:46 下午
 */
object ConversationManager {

    private val effects = ConcurrentHashMap<String, List<PotionEffect>>()
    private val effectFreeze = mapOf(PotionEffectType.BLINDNESS to 0, PotionEffectType.SLOW to 4)

    @Config("core/conversation.yml", migrate = true)
    lateinit var conf: SecuredFile
        private set

    val sessions = ConcurrentHashMap<String, Session>()

    val cooldown = Baffle.of(500, TimeUnit.MILLISECONDS)

    fun getConversation(player: Player, namespace: String, vararg name: String): Conversation? {
        val conversation = ChemdahAPI.conversation.values.firstOrNull { name.any { name -> it.isNPC(namespace, name) } }
        val event = ConversationEvents.Select(player, namespace, name.toList(), conversation)
        return if (event.call()) event.conversation else null
    }

    @Schedule(period = 1, async = true)
    internal fun onTick() {
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
                submit {
                    session.close(refuse = true)
                }
            }
        }
    }

    @Awake(LifeCycle.DISABLE)
    internal fun onDisable() {
        Bukkit.getOnlinePlayers().forEach { p ->
            if (p.conversationSession?.conversation?.hasFlag("NO_EFFECT") == false) {
                effectFreeze.forEach { p.removePotionEffect(it.key) }
                effects.remove(p.name)?.forEach { p.addPotionEffect(it) }
            }
            p.conversationSession?.close(refuse = true)
        }
    }

    @SubscribeEvent
    internal fun e(e: PlayerEvents.Released) {
        if (e.player.conversationSession?.conversation?.hasFlag("NO_EFFECT") == false) {
            effectFreeze.forEach { e.player.removePotionEffect(it.key) }
            effects.remove(e.player.name)?.forEach { e.player.addPotionEffect(it) }
        }
        e.player.conversationSession?.close(refuse = true)
        sessions.remove(e.player.name)
        cooldown.reset(e.player.name)
    }

    @Suppress("UNCHECKED_CAST")
    @SubscribeEvent
    internal fun e(e: ConversationEvents.Begin) {
        if (!e.conversation.hasFlag("NO_EFFECT")) {
            effects[e.session.player.name] = effectFreeze.mapNotNull { e.session.player.getPotionEffect(it.key) }.filter { it.duration in 10..9999 }
            effectFreeze.forEach { e.session.player.addPotionEffect(PotionEffect(it.key, 99999, it.value).hidden()) }
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
    internal fun e(e: ConversationEvents.Closed) {
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
    internal fun e(e: EntityDamageEvent) {
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
    internal fun e(e: PlayerMoveEvent) {
        if (e.player.conversationSession?.conversation?.hasFlag("NO_MOVE") == true) {
            e.setTo(e.from)
        }
    }

    @SubscribeEvent(priority = EventPriority.MONITOR, ignoreCancelled = true)
    internal fun e(e: PlayerDropItemEvent) {
        if (e.player.conversationSession?.conversation?.hasFlag("FORCE_DISPLAY") == true) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent
    internal fun e(e: PlayerCommandPreprocessEvent) {
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

    @SubscribeEvent(priority = EventPriority.MONITOR, ignoreCancelled = true)
    internal fun e(e: PlayerInteractAtEntityEvent) {
        if (e.hand == EquipmentSlot.HAND && e.player.conversationSession == null && cooldown.hasNext(e.player.name)) {
            val name = e.rightClicked.getDisplayName()
            val conversation = getConversation(e.player, "minecraft", *name) ?: return
            e.isCancelled = true
            conversation.open(e.player, object : Source<Entity>(name.last(), e.rightClicked) {

                override fun transfer(player: Player, newId: String): Boolean {
                    val entities = e.rightClicked.getNearbyEntities(10.0, 10.0, 10.0)
                    val nearby = entities.firstOrNull { it.getDisplayName().any { name -> name.equals(newId, true) } } ?: return false
                    this.name = nearby.getDisplayName().last()
                    entity = nearby
                    return true
                }

                override fun getOriginLocation(entity: Entity): Location {
                    return entity.location.add(0.0, entity.height, 0.0)
                }
            })
        }
    }

    @SubscribeEvent(bind = "com.sucy.skill.api.event.PlayerCastSkillEvent")
    internal fun onPlayerCastSkillEvent(oe: OptionalEvent) {
        val e = oe.get<PlayerCastSkillEvent>()
        if (e.player.conversationSession != null) {
            e.isCancelled = true
        }
    }

    internal fun Entity.getDisplayName(): Array<String> {
        val names = arrayListOf(type.name)
        if (customName != null) {
            names += customName!!
        }
        return names.toTypedArray()
    }

    internal object CompatAdyeshach {

        @SubscribeEvent
        fun e(e: ConversationEvents.Begin) {
            val npc = e.session.source.entity
            if (npc is EntityInstance) {
                npc.setTag("isFreeze", "true")
                npc.setTag("conversation:${e.session.player.name}", "conversation")
                // 让 NPC 看向玩家
                if (e.conversation.hasFlag("LOOK_PLAYER")) {
                    // 检查 NPC 是否记录原始视角
                    if (!npc.hasTag("conversation-eye-location")) {
                        npc.setTag("conversation-eye-location", "${npc.getLocation().yaw},${npc.getLocation().pitch}")
                    }
                    // 检查 NPC 是否持有 LookAtPlayerAlways 控制器
                    if (npc.getController(ControllerLookAtPlayerAlways::class.java) == null) {
                        npc.registerController(ControllerLookAtPlayerAlways(npc))
                        npc.setTag("conversation-controller", "true")
                    }
                }
            }
        }

        @SubscribeEvent
        fun e(e: ConversationEvents.Closed) {
            val npc = e.session.source.entity
            if (npc is EntityInstance) {
                npc.removeTag("conversation:${e.session.player.name}")
                // 若没有玩家在与该 NPC 对话
                if (npc.getTags().none { it.value == "conversation" }) {
                    // 移除冻结标记
                    npc.removeTag("isFreeze")
                    // 移除 LookAtPlayerAlways 控制器
                    if (npc.hasTag("conversation-controller")) {
                        npc.removeTag("conversation-controller")
                        npc.unregisterController(ControllerLookAtPlayerAlways::class.java)
                    }
                    // 在对话结束时恢复视角
                    if (e.session.conversation.hasFlag("LOOK_PLAYER") && npc.hasTag("conversation-eye-location")) {
                        val eye = npc.getTag("conversation-eye-location")!!.split(",")
                        npc.removeTag("conversation-eye-location")
                        npc.controllerLook(eye[0].toFloat(), eye[1].toFloat(), smooth = false)
                    }
                }
            }
        }

        @SubscribeEvent(priority = EventPriority.MONITOR, ignoreCancelled = true)
        fun e(e: AdyeshachEntityInteractEvent) {
            if (e.isMainHand && e.player.conversationSession == null && cooldown.hasNext(e.player.name)) {
                getConversation(e.player, "adyeshach", e.entity.id)?.run {
                    e.isCancelled = true
                    submit {
                        open(e.player, object : Source<EntityInstance>(e.entity.getDisplayName(), e.entity) {

                            override fun transfer(player: Player, newId: String): Boolean {
                                val nearby = e.entity.manager?.getEntities()
                                    ?.filter { it.getWorld() == player.world && it.getLocation().distance(e.entity.getLocation()) < 10.0 }
                                    ?.firstOrNull { it.id == newId } ?: return false
                                name = nearby.getDisplayName()
                                entity = nearby
                                return true
                            }

                            override fun getOriginLocation(entity: EntityInstance): Location {
                                return entity.getLocation().add(0.0, entity.entityType.entitySize.height, 0.0)
                            }
                        }) {
                            it.variables["@manager"] = e.entity.manager
                            it.variables["@entities"] = listOf(e.entity)
                        }
                    }
                }
            }
        }
    }

    internal object CompatCitizens {

        val isCitizensHooked by lazy { Bukkit.getPluginManager().isPluginEnabled("Citizens") }

        @SubscribeEvent
        fun e(e: ConversationEvents.Begin) {
            if (!isCitizensHooked) {
                return
            }
            val npc = e.session.source.entity
            if (npc is NPC && e.conversation.hasFlag("LOOK_PLAYER")) {
                npc.faceLocation(e.session.player.eyeLocation)
            }
        }

        @SubscribeEvent(priority = EventPriority.MONITOR, ignoreCancelled = true)
        fun e(e: PlayerInteractAtEntityEvent) {
            if (!isCitizensHooked) {
                return
            }
            if (e.hand == EquipmentSlot.HAND && e.rightClicked.hasMetadata("NPC") && e.player.conversationSession == null && cooldown.hasNext(e.player.name)) {
                val npc = CitizensAPI.getNPCRegistry().getNPC(e.rightClicked) ?: return
                getConversation(e.player, "citizens", npc.id.toString())?.run {
                    e.isCancelled = true
                    // 打开对话
                    open(e.player, object : Source<NPC>(npc.fullName, npc) {

                        override fun transfer(player: Player, newId: String): Boolean {
                            val nearby = e.rightClicked.getNearbyEntities(10.0, 10.0, 10.0)
                                .mapNotNull { CitizensAPI.getNPCRegistry().getNPC(e.rightClicked) }
                                .firstOrNull { it.id == Coerce.toInteger(newId) } ?: return false
                            name = npc.fullName
                            this.entity = nearby
                            return true
                        }

                        override fun getOriginLocation(entity: NPC): Location {
                            return entity.entity.location.add(0.0, entity.entity.height, 0.0)
                        }
                    })
                }
            }
        }
    }

    internal object CompatMythicMobs {

        val isMythicMobsHooked by lazy { Bukkit.getPluginManager().isPluginEnabled("MythicMobs") }

        @SubscribeEvent
        fun e(e: ConversationEvents.Begin) {
            if (!isMythicMobsHooked) {
                return
            }
            val npc = e.session.source.entity
            if (npc is ActiveMob && npc.entity.bukkitEntity is LivingEntity && e.conversation.hasFlag("LOOK_PLAYER")) {
                (npc.entity.bukkitEntity as LivingEntity).controllerLookAt(e.session.player)
            }
        }

        @SubscribeEvent(priority = EventPriority.MONITOR, ignoreCancelled = true)
        fun e(e: PlayerInteractAtEntityEvent) {
            if (!isMythicMobsHooked) {
                return
            }
            if (e.hand == EquipmentSlot.HAND && e.rightClicked is LivingEntity && e.player.conversationSession == null && cooldown.hasNext(e.player.name)) {
                val mob = MythicMobs.inst().mobManager.getMythicMobInstance(e.rightClicked) ?: return
                getConversation(e.player, "mythicmobs", mob.type.internalName)?.run {
                    e.isCancelled = true
                    // 打开对话
                    open(e.player, object : Source<ActiveMob>(mob.displayName, mob) {

                        override fun transfer(player: Player, newId: String): Boolean {
                            val nearby = e.rightClicked.getNearbyEntities(10.0, 10.0, 10.0)
                                .mapNotNull { MythicMobs.inst().mobManager.getMythicMobInstance(e.rightClicked) }
                                .firstOrNull { it.type.internalName == newId } ?: return false
                            name = nearby.displayName
                            entity = nearby
                            return true
                        }

                        override fun getOriginLocation(entity: ActiveMob): Location {
                            val be = entity.entity.bukkitEntity
                            return be.location.add(0.0, be.height, 0.0)
                        }
                    })
                }
            }
        }
    }
}