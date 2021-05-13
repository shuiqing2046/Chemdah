package ink.ptms.chemdah.module.scenes

import io.izzel.taboolib.module.nms.impl.Position
import org.bukkit.World
import org.bukkit.block.Block

/**
 * Chemdah
 * ink.ptms.chemdah.module.scenes.BlockList
 *
 * @author sky
 * @since 2021/5/13 11:55 下午
 */
interface BlockList {

    fun getList(): List<Position>

    fun getList(world: World): List<Block>
}