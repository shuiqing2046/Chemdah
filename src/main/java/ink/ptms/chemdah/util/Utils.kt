package ink.ptms.chemdah.util

import ink.ptms.chemdah.api.ChemdahAPI
import io.izzel.taboolib.cronus.util.Time
import io.izzel.taboolib.cronus.util.TimeType
import io.izzel.taboolib.kotlin.MirrorData
import io.izzel.taboolib.util.Coerce
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection

fun String.printed(separator: String = ""): List<String> {
    val result = ArrayList<String>()
    var i = 0
    while (i < length) {
        if (get(i) == '§') {
            i++
        } else {
            result.add("${substring(0, i + 1)}${if (i % 2 == 1) separator else ""}")
        }
        i++
    }
    if (separator.isNotEmpty() && i % 2 == 0) {
        result.add(this)
    }
    return result
}

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

fun mirrorDefine(id: String) {
    ChemdahAPI.mirror.define(id)
}

fun mirrorFinish(id: String) {
    ChemdahAPI.mirror.finish(id)
}

fun mirror(id: String, func: () -> Unit) {
    ChemdahAPI.mirror.check(id, func)
}