package ink.ptms.chemdah.module.scenes

import ink.ptms.chemdah.module.kether.ActionScenes.Companion.createScenesBlock
import ink.ptms.chemdah.module.kether.ActionScenes.Companion.createScenesFallingBlock
import ink.ptms.chemdah.module.kether.ActionScenes.Companion.removeScenesBlock
import ink.ptms.chemdah.util.namespaceQuest
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import taboolib.common.platform.function.adaptCommandSender
import taboolib.common.platform.function.submit
import taboolib.common.util.Vector
import taboolib.common.util.asList
import taboolib.common5.Coerce
import taboolib.library.configuration.ConfigurationSection
import taboolib.library.xseries.parseToMaterial
import taboolib.module.kether.KetherShell
import taboolib.module.kether.printKetherErrorMessage
import taboolib.platform.util.toBukkitLocation
import java.lang.Integer.max
import java.lang.Integer.min

/**
 * Chemdah
 * ink.ptms.chemdah.module.scenes.ScenesState
 *
 * @author sky
 * @since 2021/5/13 11:50 下午
 */
abstract class ScenesState(val index: Int, val root: ConfigurationSection) {

    val agent = root["$"]?.asList() ?: emptyList()
    val relative = root.getString("relative", "")!!.toVector()
    val autoNext = root.getInt("auto-next")

    abstract fun send(player: Player)

    abstract fun cancel(player: Player)

    abstract fun getAffectPosition(): Set<Vector>

    class Block(index: Int, root: ConfigurationSection, val file: ScenesFile) : ScenesState(index, root) {

        val set = root.getStringList("set").mapNotNull {
            val args = it.split(">").map { i -> i.trim() }
            if (args.size == 2) {
                val area = args[0].split("~").map { i -> i.trim() }
                val blockList = if (area.size == 1) {
                    BlockListSingle(area[0].toVector(relative))
                } else {
                    BlockListArea(area[0].toVector(relative), area[1].toVector(relative))
                }
                val block = args[1].split(" ")
                when {
                    block.size == 1 -> {
                        blockList to ScenesBlockData(block[0].split(":")[0].parseToMaterial(), Coerce.toByte(block[0].split(":").getOrNull(1)))
                    }
                    block[0] == "falling" -> {
                        blockList to ScenesBlockData(block[1].split(":")[0].parseToMaterial(), Coerce.toByte(block[1].split(":").getOrNull(1)), true)
                    }
                    else -> null
                }
            } else null
        }.toMap()

        override fun send(player: Player) {
            try {
                KetherShell.eval(agent, sender = adaptCommandSender(player), namespace = namespaceQuest)
            } catch (ex: Exception) {
                ex.printKetherErrorMessage()
            }
            set.forEach { (k, v) ->
                k.getList().forEach { pos ->
                    if (v.falling) {
                        player.createScenesFallingBlock(pos.toLocation(player.world.name).toBukkitLocation(), v.material, v.data)
                    } else {
                        player.createScenesBlock(pos.toLocation(player.world.name).toBukkitLocation(), v.material, v.data)
                    }
                }
            }
            if (autoNext > 0) {
                submit(delay = autoNext.toLong()) {
                    file.state.getOrNull(index + 1)?.send(player)
                }
            }
        }

        override fun cancel(player: Player) {
            set.forEach { (k, _) ->
                k.getList().forEach { pos ->
                    player.removeScenesBlock(pos.toLocation(player.world.name).toBukkitLocation())
                }
            }
        }

        override fun getAffectPosition(): Set<Vector> {
            return set.flatMap { (k, _) -> k.getList() }.toSet()
        }
    }

    class Copy(index: Int, root: ConfigurationSection, val file: ScenesFile) : ScenesState(index, root) {

        val fromWorld = root.getString("copy.from-world") ?: file.world
        val from = root.getString("copy.from", "")!!.run {
            BlockListArea(split("~")[0].trim().toVector(), split("~").getOrNull(1).toString().trim().toVector())
        }
        val to = root.getString("copy.to", "")!!.toVector(relative)
        val falling = root.getBoolean("copy.falling")

        override fun send(player: Player) {
            try {
                KetherShell.eval(agent, sender = adaptCommandSender(player), namespace = namespaceQuest)
            } catch (ex: Exception) {
                ex.printKetherErrorMessage()
            }
            val blocksFrom = from.getList(Bukkit.getWorld(fromWorld) ?: return)
            val blocksTo = BlockListArea(to, to.add(from.max.subtract(from.min))).getList()
            blocksFrom.forEachIndexed { index, block ->
                if (falling) {
                    player.createScenesFallingBlock(blocksTo[index].toLocation(player.world.name).toBukkitLocation(), block.type, block.data)
                } else {
                    player.createScenesBlock(blocksTo[index].toLocation(player.world.name).toBukkitLocation(), block.type, block.data)
                }
            }
            if (autoNext > 0) {
                submit(delay = autoNext.toLong()) {
                    file.state.getOrNull(index + 1)?.send(player)
                }
            }
        }

        override fun cancel(player: Player) {
            getAffectPosition().forEach {
                player.removeScenesBlock(it.toLocation(player.world.name).toBukkitLocation())
            }
        }

        override fun getAffectPosition(): Set<Vector> {
            return BlockListArea(to, to.add(from.max.subtract(from.min))).getList().toSet()
        }
    }

    companion object {

        fun String.toVector(relative: Vector? = null): Vector {
            val args = split(" ").map { Coerce.toInteger(it) }
            val position = Vector(args[0], args.getOrNull(1) ?: args[0], args.getOrNull(2) ?: args[0])
            return if (relative != null) {
                position.add(relative)
            } else {
                position
            }
        }

        fun getArea(pos1: Vector, pos2: Vector): Pair<Vector, Vector> {
            return Vector(
                min(pos1.blockX, pos2.blockX),
                min(pos1.blockY, pos2.blockY),
                min(pos1.blockZ, pos2.blockZ)
            ) to Vector(
                max(pos1.blockX, pos2.blockX),
                max(pos1.blockY, pos2.blockY),
                max(pos1.blockZ, pos2.blockZ)
            )
        }
    }
}