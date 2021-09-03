package ink.ptms.chemdah.module.realms

import ink.ptms.chemdah.module.scenes.ScenesState.Companion.toVector
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.navigation.BoundingBox
import java.lang.Double.max
import java.lang.Double.min

/**
 * Chemdah
 * ink.ptms.chemdah.module.realms.Realms
 *
 * @author sky
 * @since 2021/6/14 3:23 下午
 */
class Realms(val root: ConfigurationSection) {

    val id = root.name.toString()
    val world = root.getString("in") ?: "world"
    val area = root.getString("area", "")!!.run {
        val min = split("~")[0].trim().toVector()
        val max = split("~").getOrNull(1).toString().trim().toVector()
        BoundingBox(
            min(min.x, max.x),
            min(min.y, max.y),
            min(min.z, max.z),
            max(min.x, max.x),
            max(min.y, max.y),
            max(min.z, max.z)
        )
    }
}