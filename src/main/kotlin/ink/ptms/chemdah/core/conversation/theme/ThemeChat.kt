package ink.ptms.chemdah.core.conversation.theme

import ink.ptms.chemdah.api.ChemdahAPI.conversationSession
import ink.ptms.chemdah.api.event.collect.ConversationEvents
import ink.ptms.chemdah.core.conversation.ConversationManager
import ink.ptms.chemdah.core.conversation.PlayerReply
import ink.ptms.chemdah.core.conversation.Session
import ink.ptms.chemdah.core.quest.QuestDevelopment
import ink.ptms.chemdah.core.quest.QuestDevelopment.hasTransmitMessages
import ink.ptms.chemdah.core.quest.QuestDevelopment.releaseTransmit
import ink.ptms.chemdah.util.*
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import taboolib.common.platform.ProxyParticle
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.adaptCommandSender
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.platform.function.submit
import taboolib.common5.Coerce
import taboolib.common5.util.printed
import taboolib.library.reflex.Reflex.Companion.invokeConstructor
import taboolib.module.chat.TellrawJson
import taboolib.module.chat.colored
import taboolib.module.chat.uncolored
import taboolib.module.kether.KetherFunction
import taboolib.module.kether.extend
import taboolib.module.kether.isInt
import taboolib.module.nms.PacketSendEvent
import taboolib.module.nms.nmsClass
import taboolib.module.nms.sendPacket
import taboolib.platform.util.asLangText
import taboolib.platform.util.asLangTextOrNull
import taboolib.platform.util.toProxyLocation
import java.util.concurrent.CompletableFuture
import kotlin.text.contains

/**
 * Chemdah
 * ink.ptms.chemdah.core.conversation.ThemeChat
 *
 * @author sky
 * @since 2021/2/12 2:08 上午
 */
object ThemeChat : Theme<ThemeChatSettings>() {

    /**
     * 屏蔽其他插件在对话过程中发送的动作栏信息
     * 例如 SkillAPI
     */
    @SubscribeEvent
    private fun onPacketSend(e: PacketSendEvent) {
        if (e.packet.name == "PacketPlayOutChat" && e.packet.read<Any>("b").toString() == "GAME_INFO" && e.player.conversationSession != null) {
            val components = e.packet.read<Array<BaseComponent>>("components") ?: return
            val text = TextComponent.toPlainText(*components).uncolored()
            if (text != e.player.asLangText("theme-chat-help").uncolored()) {
                e.isCancelled = true
            }
        }
    }

    /**
     * 取消这个行为会出现客户端显示不同步的错误
     * 以及无法 mc 无法重复切换相同物品栏
     */
    @Suppress("SpellCheckingInspection")
    @SubscribeEvent(EventPriority.HIGHEST, ignoreCancelled = true)
    private fun onItemHeld(e: PlayerItemHeldEvent) {
        val session = e.player.conversationSession ?: return
        if (session.conversation.option.theme == "chat") {
            if (session.npcTalking) {
                // 是否不允许玩家跳过对话演出效果
                if (session.conversation.hasFlag("NO_SKIP", "FORCE_DISPLAY")) {
                    e.isCancelled = true
                    return
                }
                session.npcTalking = false
            }
            val replies = session.playerReplyForDisplay
            if (replies.isNotEmpty()) {
                val index = replies.indexOf(session.playerSide)
                var select: Int
                // 使用滚轮
                if (settings.useScroll) {
                    if (e.newSlot > e.previousSlot) {
                        select = index + 1
                        if (select >= replies.size) {
                            select = 0
                        }
                    } else {
                        select = index - 1
                        if (select < 0) {
                            select = replies.size - 1
                        }
                    }
                    // start linghaner
                    // fix use-scroll
                    // 1.16.5 之前的版本使用滚轮会出现客户端显示不同步的错误
                    try {
                        e.player.sendPacket(nmsClass("PacketPlayOutHeldItemSlot").invokeConstructor(e.previousSlot))
                    } catch (ignored: Throwable) {
                    }
                    // end
                    e.isCancelled = true
                } else {
                    select = e.newSlot.coerceAtMost(replies.size - 1)
                }
                if (select != index) {
                    session.playerSide = replies[select]
                    settings.playSelectSound(session)
                    CompletableFuture<Void>().npcTalk(session, session.npcSide, "", session.npcSide.size, stopAnimation = true, canReply = !session.isFarewell)
                }
            }
        }
    }

    @SubscribeEvent
    private fun onSwap(e: PlayerSwapHandItemsEvent) {
        val session = e.player.conversationSession ?: return
        if (session.conversation.option.theme == "chat") {
            e.isCancelled = true
            if (session.npcTalking) {
                // 是否不允许玩家跳过对话演出效果
                if (session.conversation.hasFlag("NO_SKIP", "FORCE_DISPLAY")) {
                    return
                }
                session.npcTalking = false
            } else {
                session.playerSide?.run { check(session).thenTrue { select(session) } }
            }
        }
    }

    @SubscribeEvent
    private fun onChat(e: AsyncPlayerChatEvent) {
        val session = e.player.conversationSession ?: return
        if (session.conversation.option.theme == "chat" && !session.npcTalking && e.message.isInt()) {
            e.isCancelled = true
            session.playerReplyForDisplay.getOrNull(Coerce.toInteger(e.message) - 1)?.run {
                check(session).thenTrue { select(session) }
            }
        }
    }

    @SubscribeEvent
    private fun onClosed(e: ConversationEvents.Closed) {
        if (e.refuse && !e.session.npcTalking && QuestDevelopment.enableMessageTransmit) {
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
            // 只有按键触发才存在默认回复修正
            if (!settings.useScroll) {
                session.playerSide = it.getOrNull(session.player.inventory.heldItemSlot.coerceAtMost(it.size - 1))
            } else {
                session.playerSide = it.getOrNull(0)
            }
            future.complete(null)
        }
        return future
    }

    override fun onBegin(session: Session): CompletableFuture<Void> {
        if (session.conversation.noFlag("NO_EFFECT:PARTICLE")) {
            ProxyParticle.CLOUD.sendTo(adaptPlayer(session.player), session.origin.clone().add(0.0, 0.5, 0.0).toProxyLocation())
        }
        return super.onBegin(session)
    }

    override fun onDisplay(session: Session, message: List<String>, canReply: Boolean): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        // 延迟
        var d = 0L
        // 取消
        var cancel = false
        // 标记为 NPC 正在发言
        session.npcTalking = true
        // 文本动画 —— 打印机效果
        val messageAnimated = message.colored().map { if (settings.animation) it.printed("_") else listOf(it) }
        // 播放信息
        messageAnimated.forEachIndexed { index, messageText ->
            messageText.forEachIndexed { printIndex, printMessage ->
                // 是否最终消息 —— 停止动画
                val stopAnimation = printIndex + 1 == messageText.size
                // 延迟发送
                submit(delay = settings.speed * d++) {
                    // 会话合法
                    if (session.isValid) {
                        // 如果 NPC 正在发言，则向玩家发送消息
                        if (session.npcTalking) {
                            future.npcTalk(session, message, printMessage, index, stopAnimation, canReply)
                        }
                        // 跳过对话
                        else if (!cancel) {
                            cancel = true
                            // 如果是告别则转发信息
                            if (session.isFarewell) {
                                session.player.releaseTransmit()
                            }
                            future.npcTalk(session, session.npcSide, printMessage = "", lineIndex = 999, stopAnimation = true, canReply = !session.isFarewell)
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
                        future.npcTalk(session, message, printMessage, index, stopAnimation, canReply, noSpace = true)
                        future.complete(null)
                    }
                }
            }
        }
        // 消息发送结束后解除标签
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
     * @param printMessage 正在打印的信息
     * @param lineIndex 打印信息在所有信息中的索引
     * @param stopAnimation 本行信息的打印是否结束
     * @param canReply 是否允许回复
     * @param noSpace 没有隔离空行
     */
    @Suppress("DuplicatedCode")
    fun CompletableFuture<Void>.npcTalk(
        session: Session,
        messages: List<String>,
        printMessage: String,
        lineIndex: Int,
        stopAnimation: Boolean,
        canReply: Boolean,
        noSpace: Boolean = false
    ) {
        // 获取有效回复
        session.conversation.playerSide.checked(session).thenApply { replies ->
            // 最终效果
            val animationStopped = lineIndex + 1 >= messages.size && stopAnimation
            // 如果是最终效果并且存在对话转发，则不发送白信息
            val json = if (noSpace || (animationStopped && session.player.hasTransmitMessages() && !canReply)) TellrawJson().fixed() else newJson()
            try {
                // 获取对话总格式 —— 识别内联脚本并继承会话变量
                val mainFormat = settings.format.map {
                    KetherFunction.parse(it, sender = adaptPlayer(session.player), namespace = namespace) { extend(session.variables) }.colored()
                }
                mainFormat.forEach { format ->
                    when {
                        // 包含标题
                        format.contains("title") -> {
                            val title = session.variables["title"]?.toString() ?: session.conversation.option.title
                            json.append(format.replace("title" to title.replace("name" to session.source.name))).newLine()
                        }
                        // 包含发言，兼容老版本 npcSide 变量
                        format.contains("npc_side", "npcSide") -> {
                            // 单行格式化
                            var mlf = session.conversation.format?.let { settings.formatLine[it] }
                            if (mlf == null && settings.formatLine.containsKey("default")) {
                                mlf = settings.formatLine["default"]!!
                            }
                            messages.colored().forEachIndexed { i, fully ->
                                when {
                                    // 已经播放
                                    lineIndex > i -> {
                                        json.append(format.replace("npc_side", "npcSide", rep = mlf?.format(fully, i, messages.size) ?: fully)).newLine()
                                    }
                                    // 正在播放
                                    lineIndex == i -> {
                                        val pm = mlf?.format(printMessage, i, messages.size) ?: printMessage
                                        json.append(format.replace("npc_side", "npcSide", rep = pm)).newLine()
                                    }
                                    // 尚未播放
                                    else -> json.newLine()
                                }
                            }
                            // 填充空行
                            val rs = replies.size.coerceAtLeast(1)
                            if (rs + messages.size < settings.spaceFilling) {
                                repeat(settings.spaceFilling - (rs + messages.size)) { json.newLine() }
                            }
                        }
                        // 包含回复
                        format.contains("reply") -> {
                            session.playerReplyForDisplay.clear()
                            session.playerReplyForDisplay.addAll(replies)
                            if (canReply) {
                                var len = 0
                                replies.forEachIndexed { idx, reply ->
                                    var newLine = false
                                    // 回复内容
                                    val text = reply.build(session)
                                    // 回复结构
                                    val rf = getReplyFormat(session, reply).replace("player_side", "playerSide", rep = text).replace("index", rep = idx + 1)
                                    // 在单行中显示回复内容
                                    if (settings.singleLineEnable) {
                                        len += text.uncolored().realLength()
                                        // 自动换行
                                        if (len >= settings.singleLineAutoSwap) {
                                            len = 0
                                            newLine = true
                                            if (animationStopped) {
                                                json.newLine()
                                            }
                                        }
                                        // 主动换行
                                        if (reply.swapLine && idx > 0) {
                                            newLine = true
                                            if (animationStopped) {
                                                json.newLine()
                                            }
                                        }
                                        // 当动画结束时，显示回复内容
                                        if (animationStopped) {
                                            json.append(format.replace("reply" to rf)).runCommand("/session reply ${reply.rid}")
                                            // 是否启用鼠标悬停显示
                                            if (settings.hoverText) {
                                                json.hoverText(text)
                                            }
                                            // 分割字符
                                            if (idx + 1 < replies.size) {
                                                json.append(settings.singleLineReplySeparator)
                                            }
                                        }
                                    } else {
                                        // 当动画结束时，显示回复内容
                                        if (animationStopped) {
                                            json.append(format.replace("reply" to rf)).runCommand("/session reply ${reply.rid}")
                                            // 是否启用鼠标悬停显示
                                            if (settings.hoverText) {
                                                json.hoverText(text)
                                            }
                                            json.newLine()
                                            newLine = true
                                        }
                                    }
                                    // 若对话未结束则显示 talking 状态
                                    if (!animationStopped) {
                                        if (settings.singleLineEnable) {
                                            if (idx == 0) {
                                                json.append(settings.talking)
                                            } else if (newLine) {
                                                json.newLine()
                                            }
                                        } else {
                                            if (idx == 0) {
                                                json.append(settings.talking).newLine()
                                            } else {
                                                json.newLine()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        // 其他
                        else -> {
                            json.append(format).newLine()
                        }
                    }
                }
            } catch (ex: Throwable) {
                ex.printStackTrace()
            }
            if (animationStopped && !canReply && QuestDevelopment.enableMessageTransmit) {
                newJson().sendTo(adaptCommandSender(session.player))
                session.player.releaseTransmit()
            }
            json.sendTo(adaptCommandSender(session.player))
            // 打印完成则结束演示
            if (animationStopped) {
                complete(null)
            }
        }
        val help = session.player.asLangTextOrNull("theme-chat-help")
        if (help?.isNotEmpty() == true) {
            adaptPlayer(session.player).sendActionBar(help)
        }
    }

    private fun getReplyFormat(session: Session, reply: PlayerReply): String {
        val format = when {
            // 自定义
            reply.format != null && settings.customSelect.containsKey(reply.format) -> settings.customSelect[reply.format]!!
            // 曾被选过
            reply.isPlayerSelected(session.player) -> settings.selected
            // 默认
            else -> settings.select
        }
        return if (session.playerSide == reply) format.select else format.other
    }

    private fun newJson(): TellrawJson {
        return TellrawJson().also { json -> repeat(settings.spaceLine) { json.newLine() } }.fixed()
    }

    private fun TellrawJson.fixed(): TellrawJson {
        return append("\n").runCommand("PLEASE!PASS!ME!d3486345-e35d-326a-b5c5-787de3814770!")
    }
}