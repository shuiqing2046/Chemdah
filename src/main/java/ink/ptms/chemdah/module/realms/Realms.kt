package ink.ptms.chemdah.module.realms

import ink.ptms.chemdah.module.scenes.ScenesState.Companion.toPosition
import ink.ptms.chemdah.util.print
import io.izzel.taboolib.kotlin.kether.KetherShell
import io.izzel.taboolib.kotlin.navigation.pathfinder.bukkit.BoundingBox
import io.izzel.taboolib.util.Coerce
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import java.lang.Double.max
import java.lang.Double.min
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.module.realms.Realms
 *
 * @author sky
 * @since 2021/6/14 3:23 下午
 */
class Realms(val root: ConfigurationSection) {

    val id = root.name
    val world = root.getString("in") ?: "world"
    val area = root.getString("area", "")!!.run {
        val min = split("~")[0].trim().toPosition()
        val max = split("~").getOrNull(1).toString().trim().toPosition()
        BoundingBox(
            min(min.x.toDouble(), max.x.toDouble()),
            min(min.y.toDouble(), max.y.toDouble()),
            min(min.z.toDouble(), max.z.toDouble()),
            max(min.x.toDouble(), max.x.toDouble()),
            max(min.y.toDouble(), max.y.toDouble()),
            max(min.z.toDouble(), max.z.toDouble())
        )
    }
}