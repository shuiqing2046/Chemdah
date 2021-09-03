package ink.ptms.chemdah.module.scenes

import org.bukkit.World
import org.bukkit.block.Block
import taboolib.common.util.Vector

/**
 * Chemdah
 * ink.ptms.chemdah.module.scenes.BlockList
 *
 * @author sky
 * @since 2021/5/13 11:55 下午
 */
interface BlockList {

    fun getList(): List<Vector>

    fun getList(world: World): List<Block>
}