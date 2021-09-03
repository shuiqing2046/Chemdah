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
class BlockListSingle(val block: Vector) : BlockList {

    override fun getList(): List<Vector> {
        return listOf(block)
    }

    override fun getList(world: World): List<Block> {
        return listOf(world.getBlockAt(block.blockX, block.blockY, block.blockZ))
    }
}