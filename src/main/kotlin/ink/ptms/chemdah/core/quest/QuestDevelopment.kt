package ink.ptms.chemdah.core.quest

import ink.ptms.chemdah.api.ChemdahAPI.conversationSession
import ink.ptms.chemdah.api.event.collect.PlayerEvents
import ink.ptms.chemdah.library.redlib.getDataContainer
import ink.ptms.chemdah.library.redlib.getDataContainerAsync
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.console
import taboolib.common.platform.function.getDataFolder
import taboolib.common5.Coerce
import taboolib.library.reflex.Reflex.Companion.getProperty
import taboolib.module.configuration.Configuration
import taboolib.module.lang.sendLang
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.PacketSendEvent
import taboolib.module.nms.sendPacket
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.QuestListener
 *
 * @author sky
 * @since 2021/6/13 12:24 上午
 */
object QuestDevelopment {

    private val playerRelease = ConcurrentHashMap<String, MutableList<String>>()
    private val playerMessageCache = ConcurrentHashMap<String, MutableList<Any>>()

    var enableUniqueBlock = false
    var enableMessageTransmit = false

    var uniqueBlockMode = UniqueBlockMode.AUTO

    init {
        val file = File(getDataFolder(), "development.yml")
        if (file.exists()) {
            val conf = Configuration.loadFromFile(file)
            enableUniqueBlock = conf.getBoolean("enable-unique-block")
            enableMessageTransmit = conf.getBoolean("enable-message-transmit")

            // 1.19 版本下该功能暂时停用
            if (MinecraftVersion.majorLegacy >= 11900) {
                enableMessageTransmit = false
                console().sendLang("console-message-transmit-not-support")
            }

            // 唯一方块模式
            uniqueBlockMode = when (conf.getString("unique-block-mode", "AUTO")!!) {
                "AUTO" -> UniqueBlockMode.AUTO
                "SQLITE" -> UniqueBlockMode.SQLITE
                "PDC" -> UniqueBlockMode.PDC
                else -> UniqueBlockMode.AUTO
            }
        }
    }

    @SubscribeEvent
    private fun onReleased(e: PlayerEvents.Released) {
        playerRelease.remove(e.player.name)
        playerMessageCache.remove(e.player.name)
    }

    @SubscribeEvent(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onBlockPlace(e: BlockPlaceEvent) {
        if (enableUniqueBlock) {
            e.block.getDataContainerAsync(true).thenAccept { data -> data["placed"] = true }
        }
    }

    @SubscribeEvent
    private fun onPacketSend(e: PacketSendEvent) {
        if (enableMessageTransmit && e.packet.name == "PacketPlayOutChat" && e.packet.read<Any>("b").toString() != "GAME_INFO") {
            var a = e.packet.read<Any>("a").toString()
            if (a == "null") {
                // 1.18 的聊天低版本的 Raw 信息可能来自其他字段
                if (MinecraftVersion.major >= 10 || MinecraftVersion.majorLegacy < 11700) {
                    kotlin.runCatching { a = Coerce.toList(e.packet.read<Any>("components")).toString() }
                } else {
                    return
                }
            }
            if (a.contains("PLEASE!PASS!ME!d3486345-e35d-326a-b5c5-787de3814770!") || playerRelease[e.player.name]?.contains(a) == true) {
                return
            }
            val message = playerMessageCache.computeIfAbsent(e.player.name) { CopyOnWriteArrayList() }
            message += e.packet.source
            if (message.size > 32) {
                message.removeFirstOrNull()
            }
            if (e.player.conversationSession?.conversation?.option?.theme == "chat") {
                e.isCancelled = true
            }
        }
    }

    fun Block.isPlaced(): Boolean {
        return if (enableUniqueBlock) getDataContainer(false)?.getBoolean("placed") == true else false
    }

    fun Player.hasTransmitMessages(): Boolean {
        return playerMessageCache.containsKey(name)
    }

    fun Player.releaseTransmit() {
        if (enableMessageTransmit) {
            val list = CopyOnWriteArrayList<String>()
            playerRelease[name] = list
            playerMessageCache[name]?.forEach { packet ->
                var value = packet.getProperty<Any>("a").toString()
                if (value == "null" && MinecraftVersion.majorLegacy < 11700) {
                    kotlin.runCatching { value = Coerce.toList(packet.getProperty<Any>("components")).toString() }
                }
                list.add(value)
                sendPacket(packet)
            }
        }
    }

    enum class UniqueBlockMode {

        AUTO, SQLITE, PDC
    }
}