package ink.ptms.chemdah.util

import ink.ptms.chemdah.Chemdah
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.potion.PotionEffect
import taboolib.common.reflect.Reflex.Companion.setProperty
import taboolib.common5.Coerce
import taboolib.common5.Demand.Companion.toDemand
import taboolib.library.configuration.ConfigurationSection
import taboolib.library.xseries.XBlock
import taboolib.library.xseries.XMaterial
import taboolib.module.configuration.SecuredFile
import taboolib.module.nms.MinecraftVersion
import taboolib.platform.util.modifyMeta

val conf: SecuredFile
    get() = Chemdah.conf

fun Any?.asMap(): Map<String, Any?> = when (this) {
    is Map<*, *> -> entries.associate { it.key.toString() to it.value }
    is ConfigurationSection -> getValues(false)
    else -> emptyMap()
}

fun Any?.asInt(def: Int = 0): Int {
    return Coerce.toInteger(this ?: def)
}

fun Any?.asDouble(def: Double = 0.0): Double {
    return Coerce.toDouble(this ?: def)
}

fun XMaterial.isBlock(block: Block): Boolean {
    return XBlock.isSimilar(block, this) && (MinecraftVersion.majorLegacy >= 11300 || block.data == data)
}

fun ItemStack.setIcon(value: String) {
    val demand = value.toDemand()
    type = XMaterial.matchXMaterial(demand.namespace).orElse(XMaterial.STONE).parseMaterial()!!
    demand.get(listOf("d", "data"))?.let {
        durability = it.asInt().toShort()
    }
    demand.get(listOf("c", "custom_data_model"))?.let {
        modifyMeta<ItemMeta> {
            setCustomModelData(it.asInt())
        }
    }
}

fun <T> safely(func: () -> T): T? {
    try {
        return func()
    } catch (ex: NoSuchFieldError) {
    } catch (ex: NoSuchMethodError) {
    } catch (ex: NoClassDefFoundError) {
    }
    return null
}

fun PotionEffect.hidden(): PotionEffect {
    if (MinecraftVersion.majorLegacy >= 11300) {
        try {
            setProperty("icon", false)
            setProperty("particles", false)
        } catch (ex: Throwable) {
        }
    }
    return this
}

fun Location.toCenter(): Location {
    val loc = clone()
    loc.x = blockX + 0.5
    loc.y = blockY + 0.5
    loc.z = blockZ + 0.5
    return loc
}