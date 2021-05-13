package ink.ptms.chemdah.module.scenes

import io.izzel.taboolib.module.nms.impl.Position
import org.bukkit.World
import org.bukkit.block.Block

/**
 * Chemdah
 * ink.ptms.chemdah.module.scenes.BlockListArae
 *
 * @author sky
 * @since 2021/5/13 11:55 下午
 */
class BlockListArea(val min: Position, val max: Position) : BlockList {

    override fun getList(): List<Position> {
        return ArrayList<Position>().also { blocks ->
            (min.x..max.x).forEach { x ->
                (min.y..max.y).forEach { y ->
                    (min.z..max.z).forEach { z ->
                        blocks.add(Position(x, y, z))
                    }
                }
            }
        }
    }

    override fun getList(world: World): List<Block> {
        return ArrayList<Block>().also { blocks ->
            (min.x..max.x).forEach { x ->
                (min.y..max.y).forEach { y ->
                    (min.z..max.z).forEach { z ->
                        blocks.add(world.getBlockAt(x, y, z))
                    }
                }
            }
        }
    }
}