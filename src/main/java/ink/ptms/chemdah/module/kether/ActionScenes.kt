package ink.ptms.chemdah.module.kether

import ink.ptms.adyeshach.api.AdyeshachAPI
import ink.ptms.adyeshach.api.event.AdyeshachEntityTickEvent
import ink.ptms.adyeshach.common.entity.EntityTypes
import ink.ptms.adyeshach.common.entity.ai.general.GeneralGravity
import ink.ptms.adyeshach.common.entity.manager.ManagerPrivateTemp
import ink.ptms.adyeshach.common.entity.type.AdyFallingBlock
import ink.ptms.chemdah.api.event.collect.PlayerEvents
import ink.ptms.chemdah.module.scenes.ScenesBlockData
import ink.ptms.chemdah.util.getPlayer
import io.izzel.taboolib.TabooLibAPI
import io.izzel.taboolib.Version
import io.izzel.taboolib.kotlin.Tasks
import io.izzel.taboolib.kotlin.kether.Kether.expects
import io.izzel.taboolib.kotlin.kether.KetherParser
import io.izzel.taboolib.kotlin.kether.ScriptParser
import io.izzel.taboolib.kotlin.kether.common.api.ParsedAction
import io.izzel.taboolib.kotlin.kether.common.api.QuestAction
import io.izzel.taboolib.kotlin.kether.common.api.QuestContext
import io.izzel.taboolib.kotlin.kether.common.loader.types.ArgTypes
import io.izzel.taboolib.module.inject.PlayerContainer
import io.izzel.taboolib.module.inject.TListener
import io.izzel.taboolib.module.inject.TSchedule
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
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerTeleportEvent
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
         * scenes set (falling) stone to location *world *0 *0 *0
         * scenes copy (falling) location *world *0 *0 *0 to location *world *0 *0 *0
         * scenes reset location *world *0 *0 *0
         *
         * scenes set location *world *0 *0 *0 falling solid stone
         */
        @KetherParser(["scenes"])
        fun parser() = ScriptParser.parser {
            when (it.expects("set", "copy", "reset")) {
                "set" -> {
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
                    val block = it.nextToken()
                    val material = Items.asMaterial(block.split(":")[0]) ?: Material.STONE
                    val data = Coerce.toByte(block.split(":").getOrNull(1))
                    it.expect("to")
                    ScenesBlockSet0(it.next(ArgTypes.ACTION), material, data, falling, solid)
                }
                "copy" -> {
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
                    val location = it.next(ArgTypes.ACTION)
                    it.expect("to")
                    ScenesBlockSet1(it.next(ArgTypes.ACTION), location, falling, solid)
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
                val position = if (Version.isAfter(Version.v1_14)) {
                    TabooLibAPI.nmsFactory().generic().fromBlockPosition(packet.reflex().read("a/c"))
                } else {
                    TabooLibAPI.nmsFactory().generic().fromBlockPosition(packet.reflex().read("a"))
                }
                val data = scenesBlocks[player.name]?.get(player.world.name)?.get(position) ?: return true
                PlayerEvents.ScenesBlockInteract(player, data).call()
                return false
            }
            if (packet.equals("PacketPlayInBlockDig")) {
                val position = TabooLibAPI.nmsFactory().generic().fromBlockPosition(packet.read("a") ?: return true)
                val data = scenesBlocks[player.name]?.get(player.world.name)?.get(position) ?: return true
                if (packet.read("c").toString() == "STOP_DESTROY_BLOCK") {
                    if (PlayerEvents.ScenesBlockBreak(player, data).call().isCancelled) {
                        Tasks.delay(1) {
                            player.createScenesBlock(position.toLocation(player.world), data.material, data.data)
                        }
                        return false
                    } else {
                        player.removeScenesBlock(position.toLocation(player.world))
                    }
                }
            }
            return true
        }

        @TSchedule(period = 40, async = true)
        fun e() {
            Bukkit.getOnlinePlayers().forEach { it.updateScenesBlock() }
        }

        @EventHandler
        fun e(e: PlayerTeleportEvent) {
            Tasks.delay(20) {
                e.player.updateScenesBlock()
            }
        }

        @EventHandler
        fun e(e: PlayerChangedWorldEvent) {
            Tasks.delay(20) {
                e.player.updateScenesBlock()
            }
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
            val worlds = scenesBlocks.computeIfAbsent(name) { ConcurrentHashMap() }
            val blocks = worlds.computeIfAbsent(world.name) { ConcurrentHashMap() }
            blocks[Position.at(location)] = ScenesBlockData(material, data)
            if (Version.isAfter(Version.v1_13)) {
                sendBlockChange(location, material.createBlockData())
            } else {
                sendBlockChange(location, material, data)
            }
        }

        fun Player.updateScenesBlock() {
            scenesBlocks[name]?.get(world.name)?.forEach {
                val loc = it.key.toLocation(world)
                if (loc.distance(location) < 128) {
                    if (Version.isAfter(Version.v1_13)) {
                        sendBlockChange(loc, it.value.material.createBlockData())
                    } else {
                        sendBlockChange(loc, it.value.material, it.value.data)
                    }
                }
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
            if (manager is ManagerPrivateTemp
                && entity is AdyFallingBlock
                && entity.hasTag("chemdah:scenes")
                && entity.getController().any { it is GeneralGravity }
                && entity.isControllerOnGround()
            ) {
                if (entity.getTag("chemdah:scenes") == "SOLID") {
                    Bukkit.getPlayerExact(manager.player)?.createScenesBlock(entity.getLocation(), entity.material, entity.data)
                }
                entity.removeTag("chemdah:scenes")
                entity.delete()
            }
        }
    }
}