package ink.ptms.chemdah.util

import ink.ptms.chemdah.Chemdah
import ink.ptms.chemdah.api.ChemdahAPI
import io.izzel.taboolib.Version
import io.izzel.taboolib.cronus.util.Time
import io.izzel.taboolib.cronus.util.TimeType
import io.izzel.taboolib.internal.xseries.XBlock
import io.izzel.taboolib.internal.xseries.XMaterial
import io.izzel.taboolib.kotlin.Demand.Companion.toDemand
import io.izzel.taboolib.kotlin.Mirror
import io.izzel.taboolib.kotlin.MirrorData
import io.izzel.taboolib.kotlin.Reflex.Companion.reflex
import io.izzel.taboolib.module.config.TConfig
import io.izzel.taboolib.util.Coerce
import io.izzel.taboolib.util.item.ItemBuilder
import io.izzel.taboolib.util.item.Items
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect

val conf: TConfig
    get() = Chemdah.conf

fun Any?.asInt(def: Int = 0) = Coerce.toInteger(this ?: def)

fun Any?.asDouble(def: Double = 0.0) = Coerce.toDouble(this ?: def)

fun Any?.asMap() = when (this) {
    is Map<*, *> -> map { (k, v) -> k.toString() to v }.toMap()
    is ConfigurationSection -> getValues(false)
    else -> emptyMap()
}

fun Any.asList(): List<String> {
    return if (this is List<*>) map { it.toString() } else listOf(toString())
}

fun String.toTime(): Time {
    val args = split(" ")
    return when (args[0]) {
        "day" -> Time(
            Coerce.toInteger(args[1]),
            Coerce.toInteger(args.getOrNull(2) ?: 0)
        )
        "week" -> Time(
            TimeType.WEEK,
            Coerce.toInteger(args[1]),
            Coerce.toInteger(args.getOrNull(2) ?: 0),
            Coerce.toInteger(args.getOrNull(3) ?: 0)
        )
        "month" -> Time(
            TimeType.MONTH,
            Coerce.toInteger(args[1]),
            Coerce.toInteger(args.getOrNull(2) ?: 0),
            Coerce.toInteger(args.getOrNull(3) ?: 0)
        )
        else -> Time(args[0])
    }.origin(this)
}

fun ItemStack.setIcon(value: String) {
    val itemBuilder = ItemBuilder(this)
    value.toDemand().run {
        Items.asMaterial(namespace).let {
            type = it
        }
        get(listOf("d", "data"))?.let {
            itemBuilder.damage(it.asInt())
        }
        get(listOf("c", "custom_data_model"))?.let {
            itemBuilder.customModelData(it.asInt())
        }
    }
    itemBuilder.build()
}

fun warning(any: Any?) {
    Bukkit.getLogger().warning("[Chemdah] $any")
}

fun mirrorFuture(id: String, func: Mirror.MirrorFuture.() -> Unit) {
    ChemdahAPI.mirror.mirrorFuture(id, func)
}

fun mirrorFinish(id: String, time: Long) {
    ChemdahAPI.mirror.dataMap.computeIfAbsent(id) { MirrorData() }.finish(time)
}

fun XMaterial.isBlock(block: Block): Boolean {
    return XBlock.isSimilar(block, this) && (Version.isAfter(Version.v1_13) || block.data == data)
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
    if (Version.isAfter(Version.v1_13)) {
        try {
            reflex("icon", false)
            reflex("particles", false)
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