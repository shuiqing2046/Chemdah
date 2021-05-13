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
class BlockListSingle(val block: Position) : BlockList {

    override fun getList(): List<Position> {
        return listOf(block)
    }

    override fun getList(world: World): List<Block> {
        return listOf(world.getBlockAt(block.x, block.y, block.z))
    }
}