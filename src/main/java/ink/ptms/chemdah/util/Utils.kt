package ink.ptms.chemdah.util

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
    return if (this !is List<*>) {
        listOf(toString())
    } else {
        map { it.toString() }
    }
}