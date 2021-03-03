package ink.ptms.chemdah.core

import ink.ptms.chemdah.util.warning
import io.izzel.taboolib.kotlin.navigation.pathfinder.bukkit.BoundingBox
import io.izzel.taboolib.util.Coerce
import org.bukkit.Bukkit
import org.bukkit.Location

/**
 * Chemdah
 * ink.ptms.chemdah.core.Cosition
 *
 * @author sky
 * @since 2021/3/2 2:51 下午
 */
abstract class CPosition(val source: String) {

    abstract fun inside(location: Location): Boolean

    /**
     * world 0 0 0 > 10 10 10
     */
    class Area(source: String) : CPosition(source) {

        val world: String
        val box: BoundingBox

        init {
            val args = source.split(" ")
            world = args[0]
            box = BoundingBox(
                Coerce.toDouble(args[1]),
                Coerce.toDouble(args[2]),
                Coerce.toDouble(args[3]),
                Coerce.toDouble(args[5]),
                Coerce.toDouble(args[6]),
                Coerce.toDouble(args[7])
            )
        }

        override fun inside(location: Location): Boolean {
            return world == location.world?.name && box.contains(location.x, location.y, location.z)
        }
    }

    /**
     * world 0 0 0 ~ 10
     */
    class Range(source: String) : CPosition(source) {

        val position: Location
        val r: Double

        init {
            val args = source.split(" ")
            position = Location(Bukkit.getWorld(args[0]), Coerce.toDouble(args[1]), Coerce.toDouble(args[2]), Coerce.toDouble(args[3]))
            r = Coerce.toDouble(args[5])
        }

        override fun inside(location: Location): Boolean {
            return position.world == location.world && position.distance(location) <= r
        }
    }

    /**
     * world 0 0 0 & 1 1 1 & 2 2 2
     */
    class Single(source: String) : CPosition(source) {

        val positions = ArrayList<Location>()

        init {
            source.split("&").forEach {
                val args = it.trim().split(" ")
                if (args.size == 3) {
                    positions += Location(null, Coerce.toDouble(args[0]), Coerce.toDouble(args[1]), Coerce.toDouble(args[2]))
                } else {
                    positions += Location(Bukkit.getWorld(args[0]), Coerce.toDouble(args[1]), Coerce.toDouble(args[2]), Coerce.toDouble(args[3]))
                }
            }
        }

        override fun inside(location: Location): Boolean {
            return positions.any {
                (it.world == null || it.world == location.world)
                        && it.blockX == location.blockX
                        && it.blockY == location.blockY
                        && it.blockZ == location.blockZ
            }
        }
    }

    class Unrecognized(source: String, val message: String): CPosition(source) {

        override fun inside(location: Location): Boolean {
            warning("Unrecognized position format: $source ($message)")
            return false
        }
    }

    companion object {

        fun String.toCosition(): CPosition {
            return try {
                when {
                    ">" in this -> Area(this)
                    "~" in this -> Range(this)
                    else -> Single(this)
                }
            } catch (e: Throwable) {
                Unrecognized(this, e.message.toString())
            }
        }
    }
}