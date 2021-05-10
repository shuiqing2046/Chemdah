package ink.ptms.chemdah.module.kether

import ink.ptms.adyeshach.api.AdyeshachAPI
import ink.ptms.adyeshach.api.event.AdyeshachEntityTickEvent
import ink.ptms.adyeshach.common.entity.EntityTypes
import ink.ptms.adyeshach.common.entity.manager.ManagerPrivateTemp
import ink.ptms.adyeshach.common.entity.type.AdyFallingBlock
import ink.ptms.chemdah.api.event.collect.PlayerEvents
import ink.ptms.chemdah.util.getPlayer
import io.izzel.taboolib.TabooLibAPI
import io.izzel.taboolib.Version
import io.izzel.taboolib.kotlin.kether.Kether.expects
import io.izzel.taboolib.kotlin.kether.KetherParser
import io.izzel.taboolib.kotlin.kether.ScriptParser
import io.izzel.taboolib.kotlin.kether.common.api.ParsedAction
import io.izzel.taboolib.kotlin.kether.common.api.QuestAction
import io.izzel.taboolib.kotlin.kether.common.api.QuestContext
import io.izzel.taboolib.kotlin.kether.common.loader.types.ArgTypes
import io.izzel.taboolib.module.inject.PlayerContainer
import io.izzel.taboolib.module.inject.TListener
import io.izzel.taboolib.module.nms.impl.Position
import io.izzel.taboolib.module.packet.Packet
import io.izzel.taboolib.module.packet.TPacket
import io.izzel.taboolib.util.Coerce
import io.izzel.taboolib.util.item.Items
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.lang.Exception
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.ActionScenes
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class ActionScenes {

    data class ScenesBlockData(val material: Material, val data: Byte = 0)

    class ScenesBlockSet0(
        val location: ParsedAction<*>,
        val material: Material,
        val data: Byte = 0,
        val falling: Boolean,
        val solid: Boolean
    ) : QuestAction<Void>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<Void> {
            return frame.newFrame(location).run<Location>().thenAccept { location ->
                if (falling) {
                    frame.getPlayer().createScenesFallingBlock(location, material, data, solid)
                } else {
                    frame.getPlayer().createScenesBlock(location, material, data)
                }
            }
        }

        override fun toString(): String {
            return "ScenesBlockSet0(location=$location, material=$material, data=$data, falling=$falling, solid=$solid)"
        }

    }

    class ScenesBlockSet1(
        val location: ParsedAction<*>,
        val copy: ParsedAction<*>,
        val falling: Boolean,
        val solid: Boolean
    ) : QuestAction<Void>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<Void> {
            return frame.newFrame(location).run<Location>().thenAccept { location ->
                frame.newFrame(copy).run<Location>().thenAccept { copy ->
                    val block = copy.block
                    if (falling) {
                        frame.getPlayer().createScenesFallingBlock(location, block.type, block.data, solid)
                    } else {
                        frame.getPlayer().createScenesBlock(location, block.type, block.data)
                    }
                }
            }
        }

        override fun toString(): String {
            return "ScenesBlockSet1(location=$location, copy=$copy, falling=$falling, solid=$solid)"
        }

    }

    class ScenesBlockReset(val location: ParsedAction<*>) : QuestAction<Void>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<Void> {
            return frame.newFrame(location).run<Location>().thenAccept { location ->
                frame.getPlayer().removeScenesBlock(location)
            }
        }

        override fun toString(): String {
            return "ScenesBlockReset(location=$location)"
        }

    }

    @TListener
    companion object : Listener {

        /**
         * scenes set location *world *0 *0 *0 to (falling) stone
         * scenes set location *world *0 *0 *0 to (falling) location *world *0 *0 *0
         * scenes reset location *world *0 *0 *0
         *
         * scenes set location *world *0 *0 *0 falling solid stone
         */
        @KetherParser(["scenes"])
        fun parser() = ScriptParser.parser {
            when (it.expects("set", "reset")) {
                "set" -> {
                    val location = it.next(ArgTypes.ACTION)
                    it.expect("to")
                    val falling = try {
                        it.mark()
                        it.expect("falling")
                        true
                    } catch (ex: Exception) {
                        it.reset()
                        false
                    }
                    val solid = if (falling) {
                        try {
                            it.mark()
                            it.expect("solid")
                            true
                        } catch (ex: Exception) {
                            it.reset()
                            false
                        }
                    } else false
                    it.mark()
                    val block = it.nextToken()
                    if (block == "location") {
                        it.reset()
                        ScenesBlockSet1(location, it.next(ArgTypes.ACTION), falling, solid)
                    } else {
                        val material = Items.asMaterial(block.split(":")[0]) ?: Material.STONE
                        val data = Coerce.toByte(block.split(":").getOrNull(1))
                        ScenesBlockSet0(location, material, data, falling, solid)
                    }
                }
                "reset" -> {
                    ScenesBlockReset(it.next(ArgTypes.ACTION))
                }
                else -> error("out of case")
            }
        }

        @PlayerContainer
        private val scenesBlocks = ConcurrentHashMap<String, MutableMap<String, MutableMap<Position, ScenesBlockData>>>()

        @TPacket(type = TPacket.Type.RECEIVE)
        private fun e(player: Player, packet: Packet): Boolean {
            if (packet.equals("PacketPlayInUseItem")) {
                val position = TabooLibAPI.nmsFactory().generic().fromBlockPosition(packet.reflex().read("a/c"))
                val data = scenesBlocks[player.name]?.get(player.world.name)?.get(position) ?: return true
                PlayerEvents.ScenesBlockInteract(player, data).call()
                return false
            }
            if (packet.equals("PacketPlayInBlockDig")) {
                val position = TabooLibAPI.nmsFactory().generic().fromBlockPosition(packet.read("a"))
                val data = scenesBlocks[player.name]?.get(player.world.name)?.get(position) ?: return true
                if (packet.read("c").toString() == "STOP_DESTROY_BLOCK") {
                    PlayerEvents.ScenesBlockBreak(player, data).call()
                    return false
                }
            }
            return true
        }

        fun Player.removeScenesBlock(location: Location) {
            scenesBlocks[name]?.get(world.name)?.remove(Position.at(location))
            if (Version.isAfter(Version.v1_13)) {
                sendBlockChange(location, location.block.blockData)
            } else {
                sendBlockChange(location, location.block.type, location.block.data)
            }
        }

        fun Player.createScenesBlock(location: Location, material: Material, data: Byte = 0) {
            scenesBlocks[name]?.get(world.name)?.put(Position.at(location), ScenesBlockData(material, data))
            if (Version.isAfter(Version.v1_13)) {
                sendBlockChange(location, material.createBlockData())
            } else {
                sendBlockChange(location, material, data)
            }
        }

        fun Player.createScenesFallingBlock(location: Location, material: Material, data: Byte = 0, toSolid: Boolean = false) {
            val manager = AdyeshachAPI.getEntityManagerPrivateTemporary(this)
            val npc = manager.create(EntityTypes.FALLING_BLOCK, location) {
                (it as AdyFallingBlock).setMaterial(material, data)
            }
            npc.setTag("chemdah:scenes", if (toSolid) "SOLID" else "NONE")
            npc.registerController(AdyeshachAPI.getKnownController("Gravity")!!.get(npc))
        }

        @EventHandler
        fun e(e: AdyeshachEntityTickEvent) {
            val entity = e.entity
            val manager = entity.manager
            if (entity is AdyFallingBlock && entity.isControllerOnGround() && entity.hasTag("chemdah:scenes") && manager is ManagerPrivateTemp) {
                if (entity.getTag("chemdah:scenes") == "SOLID") {
                    Bukkit.getPlayerExact(manager.player)?.createScenesBlock(entity.getLocation(), entity.material, entity.data)
                }
                entity.removeTag("chemdah:scenes")
                entity.delete()
            }
        }
    }
}