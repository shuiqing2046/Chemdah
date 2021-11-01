package ink.ptms.chemdah.core.quest

import ink.ptms.blockdb.Data
import ink.ptms.blockdb.createDataContainer
import ink.ptms.blockdb.getDataContainer
import ink.ptms.chemdah.api.ChemdahAPI.conversationSession
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.getDataFolder
import taboolib.common.reflect.Reflex.Companion.getProperty
import taboolib.module.configuration.SecuredFile
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
object QuestDevelopment  {

    private val playerRelease = ConcurrentHashMap<String, MutableList<String>>()

    private val playerMessageCache = ConcurrentHashMap<String, MutableList<Any>>()

    var enableUniqueBlock = false
    var enableMessageTransmit = false

    init {
        val file = File(getDataFolder(), "development.yml")
        if (file.exists()) {
            val conf = SecuredFile.loadConfiguration(file)
            enableUniqueBlock = conf.getBoolean("enable-unique-block")
            enableMessageTransmit = conf.getBoolean("enable-message-transmit")
        }
    }

    @SubscribeEvent
    fun e(e: PlayerQuitEvent) {
        playerRelease.remove(e.player.name)
        playerMessageCache.remove(e.player.name)
    }

    @SubscribeEvent(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun e(e: BlockPlaceEvent) {
        if (enableUniqueBlock) {
            (e.block.getDataContainer() ?: e.block.createDataContainer())["placed"] = Data(true)
        }
    }

    @SubscribeEvent
    fun e(e: PacketSendEvent): Boolean {
        if (enableMessageTransmit && e.packet.name == "PacketPlayOutChat" && e.packet.read<Any>("b").toString() != "GAME_INFO") {
            val a = e.packet.read<Any>("a").toString()
            if (a.contains("PLEASE!PASS!ME!d3486345-e35d-326a-b5c5-787de3814770!") || playerRelease[e.player.name]?.contains(a) == true) {
                return true
            }
            val message = playerMessageCache.computeIfAbsent(e.player.name) { CopyOnWriteArrayList() }
            message += e.packet.source
            if (message.size > 32) {
                message.removeFirstOrNull()
            }
            if (e.player.conversationSession?.conversation?.option?.theme == "chat") {
                return false
            }
        }
        return true
    }

    fun Block.isPlaced(): Boolean {
        return getDataContainer()?.get("placed")?.toBoolean() == true
    }

    fun Player.hasTransmitMessages(): Boolean {
        return playerMessageCache.containsKey(name)
    }

    fun Player.releaseTransmit() {
        if (enableMessageTransmit) {
            val list = CopyOnWriteArrayList<String>()
            playerRelease[name] = list
            playerMessageCache[name]?.forEachIndexed { index, packet ->
                // 2021/06/13 03:00
                // 因为聊天数据包会被重复拦截两次每次都不一样，1.12 和 1.16 均有该问题所以只发偶数包
                // 删除 ViaVersion 测试同样如此
                if (index % 2 == 0) {
                    val value = packet.getProperty<Any>("a")
                    if (value != null) {
                        list.add(value.toString())
                        sendPacket(packet)
                    }
                }
            }
        }
    }
}