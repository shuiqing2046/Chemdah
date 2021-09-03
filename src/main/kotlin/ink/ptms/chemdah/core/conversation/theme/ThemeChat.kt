package ink.ptms.chemdah.core.conversation.theme

import ink.ptms.chemdah.api.ChemdahAPI.conversationSession
import ink.ptms.chemdah.api.event.collect.ConversationEvents
import ink.ptms.chemdah.core.conversation.ConversationManager
import ink.ptms.chemdah.core.conversation.Session
import ink.ptms.chemdah.core.quest.QuestDevelopment
import ink.ptms.chemdah.core.quest.QuestDevelopment.hasTransmitMessages
import ink.ptms.chemdah.core.quest.QuestDevelopment.releaseTransmit
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import taboolib.common.platform.*
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.adaptCommandSender
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.platform.function.submit
import taboolib.common.reflect.Reflex.Companion.invokeMethod
import taboolib.common5.Baffle
import taboolib.common5.Coerce
import taboolib.common5.util.printed
import taboolib.module.chat.TellrawJson
import taboolib.module.chat.colored
import taboolib.module.kether.isInt
import taboolib.module.nms.PacketSendEvent
import taboolib.platform.util.asLangText
import taboolib.platform.util.toProxyLocation
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

/**
 * Chemdah
 * ink.ptms.chemdah.core.conversation.ThemeChat
 *
 * @author sky
 * @since 2021/2/12 2:08 上午
 */
object ThemeChat : Theme<ThemeChatSettings>(), Listener {

    init {
        register("chat")
    }

    val baffle = Baffle.of(100, TimeUnit.MILLISECONDS)

    /**
     * 屏蔽其他插件在对话过程中发送的动作栏信息
     * 例如 SkillAPI
     */
    @SubscribeEvent
    fun e(e: PacketSendEvent) {
        if (e.packet.name == "PacketPlayOutChat" && e.packet.read<Any>("b").toString() == "GAME_INFO") {
            if (e.player.conversationSession != null && e.packet.read<Any>("a")?.invokeMethod<String>("getText") != e.player.asLangText("theme-chat-help")) {
                e.isCancelled = true
            }
        }
    }

    /**
     * 取消这个行为会出现客户端显示不同步的错误
     * 以及无法 mc 无法重复切换相同物品栏
     */
    @SubscribeEvent
    fun e(e: PlayerItemHeldEvent) {
        val session = e.player.conversationSession ?: return
        if (session.conversation.option.theme == "chat") {
            if (session.npcTalking) {
                // 是否不允许玩家跳过对话演出效果
                if (session.conversation.hasFlag("NO_SKIP") || session.conversation.hasFlag("FORCE_DISPLAY")) {
                    e.isCancelled = true
                    return
                }
                session.npcTalking = false
            }
            val replies = session.playerReplyForDisplay
            if (replies.isNotEmpty()) {
                val index = replies.indexOf(session.playerSide)
                val select = e.newSlot.coerceAtMost(replies.size - 1)
                if (select != index) {
                    session.playerSide = replies[select]
                    settings.playSelectSound(session)
                    CompletableFuture<Void>().npcTalk(session, session.npcSide, "", session.npcSide.size, endMessage = true, canReply = !session.isFarewell)
                }
            }
        }
    }

    @SubscribeEvent
    fun e(e: PlayerSwapHandItemsEvent) {
        val session = e.player.conversationSession ?: return
        if (session.conversation.option.theme == "chat") {
            e.isCancelled = true
            if (session.npcTalking) {
                // 是否不允许玩家跳过对话演出效果
                if (session.conversation.hasFlag("NO_SKIP") || session.conversation.hasFlag("FORCE_DISPLAY")) {
                    return
                }
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

    @SubscribeEvent
    fun e(e: AsyncPlayerChatEvent) {
        val session = e.player.conversationSession ?: return
        if (session.conversation.option.theme == "chat" && !session.npcTalking && e.message.isInt()) {
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

    @SubscribeEvent
    fun e(e: ConversationEvents.Closed) {
        if (e.refust && !e.session.npcTalking && QuestDevelopment.enableMessageTransmit) {
            newJson().sendTo(adaptCommandSender(e.session.player))
            e.session.player.releaseTransmit()
        }
    }

    override fun createConfig(): ThemeChatSettings {
        return ThemeChatSettings(ConversationManager.conf.getConfigurationSection("theme-chat")!!)
    }

    override fun onReset(session: Session): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        session.conversation.playerSide.checked(session).thenApply {
            session.playerSide = it.getOrNull(session.player.inventory.heldItemSlot.coerceAtMost(it.size - 1))
            future.complete(null)
        }
        return future
    }

    override fun onBegin(session: Session): CompletableFuture<Void> {
        ProxyParticle.CLOUD.sendTo(adaptPlayer(session.player), session.origin.clone().add(0.0, 0.5, 0.0).toProxyLocation())
        return super.onBegin(session)
    }

    override fun onDisplay(session: Session, message: List<String>, canReply: Boolean): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        var d = 0L
        var cancel = false
        session.npcTalking = true
        message.colored().map { if (settings.animation) it.printed("_") else listOf(it) }.forEachIndexed { index, messageText ->
            messageText.forEachIndexed { printIndex, printText ->
                val endMessage = printIndex + 1 == messageText.size
                submit(delay = d++) {
                    if (session.isValid) {
                        if (session.npcTalking) {
                            future.npcTalk(session, message, printText, index, endMessage = endMessage, canReply = canReply)
                        }
                        // 跳过对话
                        else if (!cancel) {
                            cancel = true
                            // 如果是告别则转发信息
                            if (session.isFarewell) {
                                session.player.releaseTransmit()
                            }
                            future.npcTalk(session, session.npcSide, "", session.npcSide.size, endMessage = true, canReply = !session.isFarewell)
                            future.complete(null)
                        }
                    }
                    // 对话中断
                    else if (!cancel) {
                        cancel = true
                        if (QuestDevelopment.enableMessageTransmit) {
                            // 清空对话
                            newJson().sendTo(adaptCommandSender(session.player))
                            // 转发信息
                            session.player.releaseTransmit()
                        }
                        future.npcTalk(session, message, printText, index, endMessage = endMessage, canReply, noSpace = true)
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

    /**
     * 发送聊天信息
     *
     * @param session 会话对象
     * @param messages 所有信息
     * @param message 正在打印的信息
     * @param index 打印信息在所有信息中的索引
     * @param endMessage 本行信息的打印是否结束
     * @param canReply 是否允许回复
     * @param noSpace 没有隔离空行
     */
    fun CompletableFuture<Void>.npcTalk(
        session: Session,
        messages: List<String>,
        message: String,
        index: Int,
        endMessage: Boolean,
        canReply: Boolean,
        noSpace: Boolean = false
    ) {
        session.conversation.playerSide.checked(session).thenApply { replies ->
            // 最终效果
            val finally = index + 1 >= messages.size && endMessage
            // 如果是最终效果并且存在对话转发，则不发送白信息
            val json = if (noSpace || (finally && session.player.hasTransmitMessages() && !canReply)) TellrawJson().fixed() else newJson()
            try {
                settings.format.forEach {
                    when {
                        it.contains("{title}") -> {
                            json.append(it.replace("{title}", session.conversation.option.title.replace("{name}", session.npcName))).newLine()
                        }
                        it.contains("{npcSide}") -> {
                            messages.colored().forEachIndexed { i, fully ->
                                when {
                                    index > i -> json.append(it.replace("{npcSide}", fully)).newLine()
                                    index == i -> json.append(it.replace("{npcSide}", message)).newLine()
                                    else -> json.newLine()
                                }
                            }
                        }
                        it.contains("{playerSide}") -> {
                            session.playerReplyForDisplay.clear()
                            session.playerReplyForDisplay.addAll(replies)
                            if (canReply) {
                                replies.forEachIndexed { n, reply ->
                                    if (finally) {
                                        val text = reply.build(session)
                                        if (session.playerSide == reply) {
                                            json.append(it.replace("{select}", settings.selectChar).replace("{playerSide}", "${settings.selectColor}$text"))
                                                .hoverText(text)
                                                .runCommand("/session reply ${reply.uuid}")
                                                .newLine()
                                        } else {
                                            json.append(it.replace("{select}", settings.selectOther).replace("{playerSide}", text))
                                                .hoverText(text)
                                                .runCommand("/session reply ${reply.uuid}")
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
            if (finally && !canReply && QuestDevelopment.enableMessageTransmit) {
                newJson().sendTo(adaptCommandSender(session.player))
                session.player.releaseTransmit()
            }
            json.sendTo(adaptCommandSender(session.player))
            // 打印完成则结束演示
            if (finally) {
                complete(null)
            }
        }
        adaptPlayer(session.player).sendActionBar(session.player.asLangText("theme-chat-help"))
    }

    private fun newJson(): TellrawJson {
        return TellrawJson().also { json -> repeat(settings.spaceLine) { json.newLine() } }.fixed()
    }

    private fun TellrawJson.fixed(): TellrawJson {
        return append("\n").runCommand("PLEASE!PASS!ME!d3486345-e35d-326a-b5c5-787de3814770!")
    }
}