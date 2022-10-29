package ink.ptms.chemdah.core.bukkit

import net.minecraft.world.level.block.state.IBlockData
import net.minecraft.world.level.block.state.IBlockDataHolder
import org.bukkit.block.Block
import taboolib.module.nms.MinecraftVersion

/**
 * Chemdah
 * ink.ptms.chemdah.core.bukkit.NMSImpl
 *
 * @author 坏黑
 * @since 2022/7/18 13:44
 */
class NMSImpl : NMS() {

    override fun getBlocKData(block: Block): Map<String, Any> {
        return when {
            MinecraftVersion.majorLegacy >= 11800 -> {
                ((block.blockData as CraftBlockData19).state as IBlockDataHolder<NMSBlock, IBlockData>).values.mapKeys { it.key.name }
            }
            MinecraftVersion.majorLegacy >= 11600 -> {
                (block.blockData as CraftBlockData16).state.stateMap.mapKeys { it.key.name }
            }
            MinecraftVersion.majorLegacy >= 11400 -> {
                (block.blockData as CraftBlockData14).state.stateMap.mapKeys { it.key.a() }
            }
            MinecraftVersion.majorLegacy >= 11300 -> {
                (block.blockData as CraftBlockData13).state.stateMap.mapKeys { it.key.a() }
            }
            else -> emptyMap()
        }
    }
}

typealias NMSBlock = net.minecraft.world.level.block.Block

typealias CraftBlockData19 = org.bukkit.craftbukkit.v1_19_R1.block.data.CraftBlockData

typealias CraftBlockData16 = org.bukkit.craftbukkit.v1_16_R3.block.data.CraftBlockData

typealias CraftBlockData14 = org.bukkit.craftbukkit.v1_14_R1.block.data.CraftBlockData

typealias CraftBlockData13 = org.bukkit.craftbukkit.v1_13_R2.block.data.CraftBlockData