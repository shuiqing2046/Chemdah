package ink.ptms.chemdah.util

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey

enum class TypeFlags(val match: (String, String) -> Boolean) {

    DEFAULT({ name, value -> name == value }),

    STARTS_WITH({ name, value -> name.startsWith(value) }),

    ENDS_WITH({ name, value -> name.endsWith(value) }),

    CONTAINS({ name, value -> name.contains(value) }),

    TAG({ name, value -> value.asTags().any { it.name.equals(name, true) } }),

    ALL({ _, _ -> true });

    companion object {

        private val tagsMap = HashMap<String, Set<Material>>()

        private fun String.asTags(): Set<Material> {
            return tagsMap.computeIfAbsent(this) {
                Bukkit.getTag("block", NamespacedKey.minecraft(this), Material::class.java).values
            }
        }
    }
}