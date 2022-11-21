package ink.ptms.chemdah.util

import ink.ptms.chemdah.Chemdah
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.potion.PotionEffect
import taboolib.common.util.asList
import taboolib.common5.Coerce
import taboolib.common5.Demand.Companion.toDemand
import taboolib.library.configuration.ConfigurationSection
import taboolib.library.reflex.Reflex.Companion.setProperty
import taboolib.library.xseries.XBlock
import taboolib.library.xseries.XMaterial
import taboolib.module.configuration.Configuration
import taboolib.module.nms.MinecraftVersion
import taboolib.platform.type.BukkitProxyEvent
import taboolib.platform.util.modifyMeta

val conf: Configuration
    get() = Chemdah.conf

/**
 * 将任意 [Map] 或 [Configuration] 转换为 [Map<String, Any?>]
 *
 * @return [Map<String, Any?>]
 */
fun Any?.asMap(): Map<String, Any?> = when (this) {
    is Map<*, *> -> entries.associate { it.key.toString() to it.value }
    is ConfigurationSection -> getValues(false)
    else -> emptyMap()
}

/**
 * 将对象转换为整型
 *
 * @param def 默认值
 * @return [Int]
 */
fun Any?.asInt(def: Int = 0): Int {
    return Coerce.toInteger(this ?: def)
}

/**
 * 将对象转换为浮点型
 *
 * @param def 默认值
 * @return [Double]
 */
fun Any?.asDouble(def: Double = 0.0): Double {
    return Coerce.toDouble(this ?: def)
}

/**
 * 判定材质是否为方块
 *
 * @param block 材质
 * @return [Boolean]
 */
fun XMaterial.isBlock(block: Block): Boolean {
    return XBlock.isSimilar(block, this) && (MinecraftVersion.majorLegacy >= 11300 || block.data == data)
}

/**
 * 根据给出的 icon 表达式对 [ItemStack] 进行修改
 *
 * @param value icon 表达式
 */
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

/**
 * 安全调用
 *
 * @param T 返回值类型
 * @param func 调用函数
 * @receiver [Any]
 * @return [T]
 */
fun <T> safely(func: () -> T): T? {
    try {
        return func()
    } catch (_: NoSuchFieldError) {
    } catch (_: NoSuchFileException) {
    } catch (_: NoSuchMethodError) {
    } catch (_: NoSuchMethodException) {
    } catch (_: NoClassDefFoundError) {
    }
    return null
}

/**
 * 隐藏 [PotionEffect] 的效果
 *
 * @return [PotionEffect]
 */
fun PotionEffect.hidden(): PotionEffect {
    if (MinecraftVersion.majorLegacy >= 11300) {
        try {
            setProperty("icon", false)
            setProperty("particles", false)
        } catch (_: Throwable) {
        }
    }
    return this
}

/**
 * 获取坐标中心点
 *
 * @return [Location]
 */
fun Location.toCenter(): Location {
    val loc = clone()
    loc.x = blockX + 0.5
    loc.y = blockY + 0.5
    loc.z = blockZ + 0.5
    return loc
}

/**
 * 获取字符串的真实长度（对中文进行处理）
 *
 * @return [Int]
 */
fun String.realLength(): Int {
    val regex = "[\u3091-\uFFe5]".toRegex()
    return sumBy { if (it.toString().matches(regex)) 2 else 1 }
}

/**
 * 替换字符串中的变量
 *
 * @param vars 变量
 * @return [String]
 */
fun String.replace(vararg vars: Pair<String, Any>): String {
    var r = this
    vars.forEach { r = r.replace("[\\[{]${it.first}[]}]".toRegex(), it.second.toString()) }
    return r
}

fun String.replace(vararg key: String, rep: Any): String {
    var r = this
    key.forEach { r = r.replace("[\\[{]${it}[]}]".toRegex(), rep.toString()) }
    return r
}

fun String.startsWith(vararg prefix: String): Boolean {
    return prefix.any { startsWith(it) }
}

fun String.substringAfter(vararg morePrefix: String): String {
    return substringAfter(morePrefix.firstOrNull { startsWith(it) } ?: return this)
}

fun String.contains(vararg value: String): Boolean {
    return value.any { indexOf(it) != -1 }
}

fun <K, V, M : MutableMap<in K, in V>> Iterable<Couple<K, V>>.toMap(destination: M): M {
    return destination.apply { putAll(this@toMap) }
}

fun <K, V> Iterable<Couple<K, V>>.toMap(): Map<K, V> {
    if (this is Collection) {
        return when (size) {
            0 -> emptyMap()
            1 -> mapOf(if (this is List) this[0] else iterator().next())
            else -> toMap(LinkedHashMap())
        }
    }
    return toMap(LinkedHashMap())
}

fun <K, V> MutableMap<in K, in V>.putAll(couples: Iterable<Couple<K, V>>) {
    for ((key, value) in couples) {
        put(key, value)
    }
}

fun <K, V> mapOf(couple: Couple<K, V>): Map<K, V> {
    return java.util.Collections.singletonMap(couple.key, couple.value)
}

fun Location.finite(): Location {
    if (!x.isFinite()) x = 0.0
    if (!y.isInfinite()) y = 0.0
    if (!z.isInfinite()) z = 0.0
    if (!yaw.isInfinite()) yaw = 0.0f
    if (!pitch.isInfinite()) pitch = 0.0f
    return this
}

fun BukkitProxyEvent.callIfFailed(): Boolean {
    return !call()
}

fun <V> ConfigurationSection.mapSection(transform: (ConfigurationSection) -> V): Map<String, V> {
    return getKeys(false).associateWith { transform(getConfigurationSection(it)!!) }
}

fun <V> ConfigurationSection.mapSection(node: String, transform: (ConfigurationSection) -> V): Map<String, V> {
    return getConfigurationSection(node)?.mapSection(transform) ?: emptyMap()
}

fun ConfigurationSection.getString(vararg path: String): String? {
    path.forEach {
        val r = getString(it)
        if (r != null) return r
    }
    return null
}

fun ConfigurationSection.list(path: String): MutableList<String> {
    return get(path)?.asList()?.toMutableList() ?: arrayListOf()
}

fun <T> ConfigurationSection.mapListAs(path: String, transform: (Map<String, Any?>) -> T): MutableList<T> {
    return getMapList(path).map { transform(it.asMap()) }.toMutableList()
}

fun <K, T> ConfigurationSection.sectionAs(path: String, kf: (String) -> K, transform: (K, Any) -> T): MutableList<T> {
    return getConfigurationSection(path)?.getValues(false)?.mapKeys { kf(it.key) }?.map { transform(it.key, it.value!!) }?.toMutableList() ?: arrayListOf()
}

fun List<String>.flatLines(): List<String> {
    return flatMap { it.lines() }
}

fun List<String>.trim(): List<String> {
    return map { it.trim() }
}

fun <T, R> T.to(transform: (T) -> R): R {
    return transform(this)
}