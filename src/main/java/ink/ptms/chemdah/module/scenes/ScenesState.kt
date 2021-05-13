package ink.ptms.chemdah.module.scenes

import ink.ptms.chemdah.util.asList
import io.izzel.taboolib.module.nms.impl.Position
import io.izzel.taboolib.util.Coerce
import io.izzel.taboolib.util.item.Items
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection

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

    abstract fun getAffectPosition(): Set<Position>

    class Block(index: Int, root: ConfigurationSection) : ScenesState(index, root) {

        val set = root.getStringList("root").mapNotNull {
            val args = it.split("->")
            if (args.size == 2) {
                val area = args[0].split("~")
                val blockList = if (area.size == 1) {
                    BlockListSingle(area[0].toPosition(relative))
                } else {
                    BlockListArea(args[0].toPosition(relative), args[1].toPosition(relative))
                }
                val material = Items.asMaterial(args[1].split(":")[0]) ?: Material.STONE
                val data = Coerce.toByte(args[1].split(":").getOrNull(1))
                blockList to ScenesBlockData(material, data)
            } else null
        }.toMap()

        override fun getAffectPosition(): Set<Position> {
            return set.flatMap { (k, _) -> k.getList() }.toSet()
        }
    }

    class Copy(index: Int, root: ConfigurationSection) : ScenesState(index, root) {

        val fromWorld = root.getString("copy.from-world")
        val from = root.getString("copy.from", "")!!.run { BlockListArea(split("~")[0].toPosition(), split("~").getOrNull(1).toString().toPosition()) }
        val to = root.getString("copy.to", "")!!.toPosition(relative)

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