package ink.ptms.chemdah.module.scenes

import ink.ptms.chemdah.module.kether.ActionScenes.Companion.createScenesBlock
import ink.ptms.chemdah.module.kether.ActionScenes.Companion.createScenesFallingBlock
import ink.ptms.chemdah.module.kether.ActionScenes.Companion.removeScenesBlock
import ink.ptms.chemdah.util.asList
import ink.ptms.chemdah.util.namespaceQuest
import ink.ptms.chemdah.util.print
import io.izzel.taboolib.kotlin.Tasks
import io.izzel.taboolib.kotlin.kether.KetherShell
import io.izzel.taboolib.module.nms.impl.Position
import io.izzel.taboolib.util.Coerce
import io.izzel.taboolib.util.item.Items
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player

/**
 * Chemdah
 * ink.ptms.chemdah.module.scenes.ScenesState
 *
 * @author sky
 * @since 2021/5/13 11:50 下午
 */
abstract class ScenesState(val index: Int, val root: ConfigurationSection) {

    val agent = root["$"]?.asList() ?: emptyList()
    val relative = root.getString("relative", "")!!.toPosition()
    val autoNext = root.getInt("auto-next")

    abstract fun send(player: Player)

    abstract fun cancel(player: Player)

    abstract fun getAffectPosition(): Set<Position>

    class Block(index: Int, root: ConfigurationSection, val file: ScenesFile) : ScenesState(index, root) {

        val set = root.getStringList("root").mapNotNull {
            val args = it.split(">")
            if (args.size == 2) {
                val area = args[0].split("~")
                val blockList = if (area.size == 1) {
                    BlockListSingle(area[0].toPosition(relative))
                } else {
                    BlockListArea(args[0].toPosition(relative), args[1].toPosition(relative))
                }
                val block = args[1].split(" ")
                when {
                    block.size == 1 -> {
                        blockList to ScenesBlockData(Items.asMaterial(block[0].split(":")[0]), Coerce.toByte(block[0].split(":").getOrNull(1)))
                    }
                    block[0] == "falling" -> {
                        blockList to ScenesBlockData(Items.asMaterial(block[1].split(":")[0]), Coerce.toByte(block[1].split(":").getOrNull(1)), true)
                    }
                    else -> null
                }
            } else null
        }.toMap()

        override fun send(player: Player) {
            try {
                KetherShell.eval(agent, namespace = listOf("chemdah")) {
                    sender = player
                }
            } catch (ex: Exception) {
                ex.print()
            }
            set.forEach { (k, v) ->
                k.getList().forEach { pos ->
                    if (v.falling) {
                        player.createScenesFallingBlock(pos.toLocation(player.world), v.material, v.data)
                    } else {
                        player.createScenesBlock(pos.toLocation(player.world), v.material, v.data)
                    }
                }
            }
            if (autoNext > 0) {
                Tasks.delay(autoNext.toLong()) {
                    file.state.getOrNull(index + 1)?.send(player)
                }
            }
        }

        override fun cancel(player: Player) {
            set.forEach { (k, _) ->
                k.getList().forEach { pos ->
                    player.removeScenesBlock(pos.toLocation(player.world))
                }
            }
        }

        override fun getAffectPosition(): Set<Position> {
            return set.flatMap { (k, _) -> k.getList() }.toSet()
        }
    }

    class Copy(index: Int, root: ConfigurationSection, val file: ScenesFile) : ScenesState(index, root) {

        val fromWorld = root.getString("copy.from-world") ?: file.world
        val from = root.getString("copy.from", "")!!.run { BlockListArea(split("~")[0].toPosition(), split("~").getOrNull(1).toString().toPosition()) }
        val to = root.getString("copy.to", "")!!.toPosition(relative)
        val falling = root.getBoolean("copy.falling")

        override fun send(player: Player) {
            try {
                KetherShell.eval(agent, namespace = listOf("chemdah")) {
                    sender = player
                }
            } catch (ex: Exception) {
                ex.print()
            }
            val blocksFrom = from.getList(Bukkit.getWorld(fromWorld) ?: return)
            val blocksTo = BlockListArea(to, from.max).getList()
            blocksFrom.forEachIndexed { index, block ->
                if (falling) {
                    player.createScenesFallingBlock(blocksTo[index].toLocation(player.world), block.type, block.data)
                } else {
                    player.createScenesBlock(blocksTo[index].toLocation(player.world), block.type, block.data)
                }
            }
            if (autoNext > 0) {
                Tasks.delay(autoNext.toLong()) {
                    file.state.getOrNull(index + 1)?.send(player)
                }
            }
        }

        override fun cancel(player: Player) {
            getAffectPosition().forEach {
                player.removeScenesBlock(it.toLocation(player.world))
            }
        }

        override fun getAffectPosition(): Set<Position> {
            return BlockListArea(to, from.max).getList().toSet()
        }
    }

    companion object {

        fun String.toPosition(relative: Position? = null): Position {
            val args = split(" ").map { Coerce.toInteger(it) }
            val position = Position(args[0], args.getOrNull(1) ?: args[0], args.getOrNull(2) ?: args[0])
            if (relative != null) {
                return Position(position.x + relative.x, position.y + relative.y, position.z + relative.z)
            }
            return position
        }
    }
}