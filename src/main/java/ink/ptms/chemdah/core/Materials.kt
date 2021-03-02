package ink.ptms.chemdah.core

import io.izzel.taboolib.Version
import io.izzel.taboolib.internal.xseries.XBlock
import io.izzel.taboolib.internal.xseries.XMaterial
import org.bukkit.block.Block

/**
 * Chemdah
 * ink.ptms.chemdah.core.Materials
 *
 * @author sky
 * @since 2021/3/2 5:41 下午
 */
class Materials(val materials: List<XMaterial>) {

    fun isBlock(block: Block) = materials.any { it.isBlock(block) }

    companion object {

        fun XMaterial.isBlock(block: Block): Boolean {
            return XBlock.isSimilar(block, this) && (Version.isAfter(Version.v1_13) || block.data == data)
        }

        fun List<XMaterial>.toMaterials(): Materials {
            return Materials(this)
        }
    }
}