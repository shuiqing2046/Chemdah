package ink.ptms.chemdah.module.scenes

import org.bukkit.World
import org.bukkit.block.Block
import taboolib.common.util.Vector

/**
 * Chemdah
 * ink.ptms.chemdah.module.scenes.BlockListArae
 *
 * @author sky
 * @since 2021/5/13 11:55 下午
 */
class BlockListArea(min: Vector, max: Vector) : BlockList {

    val min = ScenesState.getArea(min, max).first
    val max = ScenesState.getArea(min, max).second

    override fun getList(): List<Vector> {
        return ArrayList<Vector>().also { blocks ->
            (min.blockX..max.blockX).forEach { x ->
                (min.blockY..max.blockY).forEach { y ->
                    (min.blockZ..max.blockZ).forEach { z ->
                        blocks.add(Vector(x, y, z))
                    }
                }
            }
        }
    }

    override fun getList(world: World): List<Block> {
        return ArrayList<Block>().also { blocks ->
            (min.blockX..max.blockX).forEach { x ->
                (min.blockY..max.blockY).forEach { y ->
                    (min.blockZ..max.blockZ).forEach { z ->
                        blocks.add(world.getBlockAt(x, y, z))
                    }
                }
            }
        }
    }
}