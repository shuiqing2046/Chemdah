package ink.ptms.chemdah.core.quest

import ink.ptms.chemdah.Chemdah
import ink.ptms.chemdah.api.ChemdahAPI.conversationSession
import ink.ptms.chemdah.api.event.collect.ConversationEvents
import io.izzel.taboolib.kotlin.Reflex.Companion.reflex
import io.izzel.taboolib.kotlin.blockdb.BlockFactory.createDataContainer
import io.izzel.taboolib.kotlin.blockdb.BlockFactory.getDataContainer
import io.izzel.taboolib.kotlin.blockdb.Data
import io.izzel.taboolib.module.db.local.SecuredFile
import io.izzel.taboolib.module.inject.PlayerContainer
import io.izzel.taboolib.module.inject.TListener
import io.izzel.taboolib.module.packet.Packet
import io.izzel.taboolib.module.packet.TPacket
import io.izzel.taboolib.module.packet.TPacketHandler
import io.izzel.taboolib.module.tellraw.TellrawJson
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.QuestListener
 *
 * @author sky
 * @since 2021/6/13 12:24 上午
 */
@TListener
object QuestDevelopment : Listener {

    @PlayerContainer
    private val playerRelease = ConcurrentHashMap<String, MutableList<String>>()

    @PlayerContainer
    private val playerMessageCache = ConcurrentHashMap<String, MutableList<Any>>()

    var enableUniqueBlock = false
    var enableMessageTransmit = false

    init {
        val file = File(Chemdah.plugin.dataFolder, "development.yml")
        if (file.exists()) {
            val conf = SecuredFile.loadConfiguration(file)
            enableUniqueBlock = conf.getBoolean("enable-unique-block")
            enableMessageTransmit = conf.getBoolean("enable-message-transmit")
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun e(e: BlockPlaceEvent) {
        if (enableUniqueBlock) {
            (e.block.getDataContainer() ?: e.block.createDataContainer())["placed"] = Data(true)
        }
    }

    @TPacket(type = TPacket.Type.SEND)
    fun e(player: Player, packet: Packet): Boolean {
        if (enableMessageTransmit && packet.`is`("PacketPlayOutChat") && packet.read("b").toString() != "GAME_INFO") {
            val a = packet.read("a")!!.toString()
            if (a.contains("PLEASE!PASS!ME!d3486345-e35d-326a-b5c5-787de3814770!") || playerRelease[player.name]?.contains(a) == true) {
                return true
            }
            val message = playerMessageCache.computeIfAbsent(player.name) { ArrayList() }
            message.add(packet.get())
            if (message.size > 32) {
                message.removeLastOrNull()
            }
            if (player.conversationSession?.conversation?.option?.theme == "chat") {
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
            val list = ArrayList<String>()
            playerRelease[name] = list
            playerMessageCache[name]?.forEachIndexed { index, packet ->
                // 2021/06/13 03:00
                // 因为聊天数据包会被重复拦截两次，1.12 和 1.16 均由该问题，所以直发偶数包
                if (index % 2 == 0) {
                    list.add(packet.reflex<Any>("a")!!.toString())
                    TPacketHandler.sendPacket(this, packet)
                }
            }
        }
    }
}