package ink.ptms.chemdah.core.bukkit

import org.bukkit.block.Block
import taboolib.module.nms.nmsProxy

/**
 * Chemdah
 * ink.ptms.chemdah.core.bukkit.NMS
 *
 * @author 坏黑
 * @since 2022/7/18 13:40
 */
abstract class NMS {

    abstract fun getBlocKData(block: Block): Map<String, Any>

    companion object {

        @JvmStatic
        val INSTANCE = nmsProxy<NMS>()
    }
}