package ink.ptms.chemdah.util

import ink.ptms.chemdah.Chemdah
import ink.ptms.chemdah.api.ChemdahAPI
import io.izzel.taboolib.cronus.util.Time
import io.izzel.taboolib.cronus.util.TimeType
import io.izzel.taboolib.kotlin.Mirror
import io.izzel.taboolib.module.config.TConfig
import io.izzel.taboolib.util.Coerce
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection

val conf: TConfig
    get() = Chemdah.conf

fun Any.asMap() = when (this) {
    is Map<*, *> -> this.map { (k, v) -> k.toString() to v }.toMap()
    is ConfigurationSection -> this.getValues(false)
    else -> null
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

fun warning(any: Any?) {
    Bukkit.getLogger().warning("[Chemdah] $any")
}

fun mirrorFuture(id: String, func: Mirror.MirrorFuture.() -> Unit) {
    ChemdahAPI.mirror.mirrorFuture(id, func)
}