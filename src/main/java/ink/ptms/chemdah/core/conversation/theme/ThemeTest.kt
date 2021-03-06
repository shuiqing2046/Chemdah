package ink.ptms.chemdah.core.conversation.theme

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.core.conversation.ConversationManager
import ink.ptms.chemdah.core.conversation.Session
import io.izzel.taboolib.cronus.CronusUtils
import io.izzel.taboolib.kotlin.Tasks
import io.izzel.taboolib.kotlin.toPrinted
import io.izzel.taboolib.module.inject.TListener
import io.izzel.taboolib.module.locale.TLocale
import io.izzel.taboolib.module.tellraw.TellrawJson
import io.izzel.taboolib.util.Coerce
import io.izzel.taboolib.util.lite.Effects
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

@TListener
object ThemeTest : Theme, Listener {

    init {
        ChemdahAPI.conversationTheme["test"] = this
    }

    private val effects = ConcurrentHashMap<String, List<PotionEffect>>()
    private val effectFreeze = setOf(PotionEffectType.BLINDNESS to 0, PotionEffectType.SLOW to 4)
    private lateinit var settings: ThemeTestSettings

    @EventHandler
    fun e(e: PlayerQuitEvent) {
        effects.remove(e.player.name)?.run {
            forEach {
                e.player.addPotionEffect(it)
            }
        }
    }

    @EventHandler
    fun e(e: PlayerItemHeldEvent) {
        val session = ChemdahAPI.getConversationSession(e.player) ?: return
        if (session.conversation.option.theme == "test" && !session.npcTalking) {
            val replies = session.playerReplyForDisplay
            if (replies.isNotEmpty()) {
                val index = replies.indexOf(session.playerSide)
                val select = e.newSlot.coerceAtMost(replies.size - 1)
                if (select != index) {
                    session.playerSide = replies[select]
                    CompletableFuture<Void>().run {
                        npcTalk(session, session.npcSide, session.npcSide.size, "", printEnd = true, canReply = true)
                    }
                }
            }
        }
    }

    @EventHandler
    fun e(e: PlayerSwapHandItemsEvent) {
        val session = ChemdahAPI.getConversationSession(e.player) ?: return
        if (session.conversation.option.theme == "test") {
            e.isCancelled = true
            if (session.npcTalking) {
                session.npcTalking = false
            } else {
                session.playerSide?.run {
                    check(session).thenApply {
                        if (it) {
                            select(session)
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    fun e(e: AsyncPlayerChatEvent) {
        val session = ChemdahAPI.getConversationSession(e.player) ?: return
        if (session.conversation.option.theme == "test" && !session.npcTalking && CronusUtils.isInt(e.message)) {
            e.isCancelled = true
            session.playerReplyForDisplay.getOrNull(Coerce.toInteger(e.message) - 1)?.run {
                check(session).thenApply {
                    if (it) {
                        select(session)
                    }
                }
            }
        }
    }

    override fun reloadConfig() {
        settings = ThemeTestSettings(ConversationManager.conf.getConfigurationSection("theme-test")!!)
    }

    override fun reload(session: Session): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        session.conversation.playerSide.checked(session).thenApply {
            session.playerSide = it.getOrNull(session.player.inventory.heldItemSlot.coerceAtMost(it.size - 1))
            future.complete(null)
        }
        return future
    }

    override fun begin(session: Session): CompletableFuture<Void> {
        Effects.create(Particle.CLOUD, session.origin.clone().add(0.0, 1.5, 0.0)).count(5).player(session.player).play()
        session.player.playSound(session.origin, Sound.ENTITY_ITEM_PICKUP, 1f, 0f)
        effects[session.player.name] = effectFreeze.mapNotNull { session.player.getPotionEffect(it.first) }.filter { it.duration in 10..9999 }
        effectFreeze.forEach { session.player.addPotionEffect(PotionEffect(it.first, 99999, it.second)) }
        return npcTalk(session, session.npcSide)
    }

    override fun end(session: Session): CompletableFuture<Void> {
        effectFreeze.forEach { session.player.removePotionEffect(it.first) }
        effects.remove(session.player.name)?.forEach { session.player.addPotionEffect(it) }
        // 视觉效果
        if (!session.player.hasPotionEffect(PotionEffectType.BLINDNESS)) {
            session.player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 20, 0))
        }
        return CompletableFuture.completedFuture(null)
    }

    override fun npcTalk(session: Session, message: List<String>, canReply: Boolean): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        val messages = TLocale.Translate.setColored(message)
        var d = 0L
        var cancel = false
        session.npcTalking = true
        messages.map { it.toPrinted("_") }.forEachIndexed { messageLine, messageText ->
            messageText.forEachIndexed { printLine, printText ->
                Tasks.delay(d++) {
                    if (session.isValid) {
                        if (session.npcTalking) {
                            future.npcTalk(session, messages, messageLine, printText, printLine + 1 == messageText.size, canReply)
                        } else if (!cancel) {
                            cancel = true
                            future.npcTalk(session, session.npcSide, session.npcSide.size, "", printEnd = true, canReply = true)
                            future.complete(null)
                        }
                    } else if (!cancel) {
                        cancel = true
                        future.complete(null)
                    }
                }
            }
        }
        future.thenAccept {
            session.npcTalking = false
        }
        if (d == 0L) {
            future.complete(null)
        }
        return future
    }

    fun CompletableFuture<Void>.npcTalk(
        session: Session,
        messages: List<String>,
        messageLine: Int,
        printText: String,
        printEnd: Boolean,
        canReply: Boolean
    ) {
        session.conversation.playerSide.checked(session).thenApply { replies ->
            newJson().also { json ->
                try {
                    settings.format.forEach {
                        when {
                            it.contains("{title}") -> {
                                json.append(it.replace("{title}", session.conversation.option.title.replace("{name}", session.npcName))).newLine()
                            }
                            it.contains("{npcSide}") -> {
                                messages.forEachIndexed { i, fully ->
                                    when {
                                        messageLine > i -> json.append(it.replace("{npcSide}", fully)).newLine()
                                        messageLine == i -> json.append(it.replace("{npcSide}", printText)).newLine()
                                        else -> json.newLine()
                                    }
                                }
                            }
                            it.contains("{playerSide}") -> {
                                session.playerReplyForDisplay.clear()
                                session.playerReplyForDisplay.addAll(replies)
                                if (canReply) {
                                    replies.forEachIndexed { n, reply ->
                                        if (messageLine + 1 >= messages.size && printEnd) {
                                            val text = reply.text(session)
                                            if (session.playerSide == reply) {
                                                json.append(it.replace("{select}", settings.selectChar).replace("{playerSide}", "${settings.selectColor}$text"))
                                                    .hoverText(text)
                                                    .clickCommand("/session reply ${reply.uuid}")
                                                    .newLine()
                                            } else {
                                                json.append(it.replace("{select}", settings.selectOther).replace("{playerSide}", text))
                                                    .hoverText(text)
                                                    .clickCommand("/session reply ${reply.uuid}")
                                                    .newLine()
                                            }
                                        } else {
                                            if (n == 0) {
                                                json.append(settings.talking).newLine()
                                            } else {
                                                json.newLine()
                                            }
                                        }
                                    }
                                }
                            }
                            else -> {
                                json.append(it).newLine()
                            }
                        }
                    }
                } catch (ex: Throwable) {
                    ex.printStackTrace()
                }
            }.send(session.player)
            if (messageLine + 1 == messages.size && printEnd) {
                complete(null)
            }
        }
        TLocale.sendTo(session.player, "theme-test-help")
    }

    private fun newJson(): TellrawJson {
        return TellrawJson.create().also { json ->
            repeat(100) { json.newLine() }
        }
    }
}