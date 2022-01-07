package ink.ptms.chemdah.util

import com.google.common.base.Enums
import org.bukkit.Location
import org.bukkit.entity.Player
import taboolib.common.platform.ProxyParticle
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.util.Vector
import taboolib.common5.Coerce
import taboolib.common5.Demand
import taboolib.common5.Quat
import java.awt.Color

open class Effect(val source: String) {

    val demand = Demand(source)
    val particle = Enums.getIfPresent(ProxyParticle::class.java, demand.namespace.uppercase()).or(ProxyParticle.FLAME)!!
    val offsetX = Coerce.toDouble(demand.get(0, "0")!!)
    val offsetY = Coerce.toDouble(demand.get(1, "0")!!)
    val offsetZ = Coerce.toDouble(demand.get(2, "0")!!)
    val posX = Coerce.toDouble(demand.get("posX"))
    val posY = Coerce.toDouble(demand.get("posY"))
    val posZ = Coerce.toDouble(demand.get("posZ"))
    val speed = Coerce.toDouble(demand.get(listOf("speed", "s"), "0")!!)
    val count = Coerce.toInteger(demand.get(listOf("count", "c"), "1")!!)
    var data: ProxyParticle.Data? = null

    init {
        demand.get("block")?.let {
            data = ProxyParticle.BlockData(it)
        }
        demand.get("item")?.let {
            data = ProxyParticle.ItemData(it.split(":")[0], Coerce.toInteger(it.split(":").getOrElse(1) { "0" }))
        }
        demand.get("color")?.let {
            val color = it.split("~")[0].split("-")
            data = ProxyParticle.DustData(
                Color(
                    Coerce.toInteger(color.getOrElse(0) { "0" }),
                    Coerce.toInteger(color.getOrElse(1) { "1" }),
                    Coerce.toInteger(color.getOrElse(2) { "2" })
                ),
                Coerce.toFloat(it.split("~").getOrElse(1) { "0" })
            )
        }
    }

    fun run(location: Location) {
        val pos = location.clone().add(posX, posY, posZ)
        var quat = Quat.at(pos.x, pos.y, pos.z)
        quat = quat.rotate2D(location.yaw.toDouble(), location.x, location.z)
        location.world!!.getNearbyEntities(location, 100.0, 100.0, 100.0).filterIsInstance<Player>().forEach {
            particle.sendTo(
                adaptPlayer(it),
                taboolib.common.util.Location(location.world!!.name, quat.x(), quat.y(), quat.z()),
                Vector(offsetX, offsetY, offsetZ),
                count,
                speed,
                data,
            )
        }
    }

    fun run(location: Location, player: Player) {
        val pos = location.clone().add(posX, posY, posZ)
        var quat = Quat.at(pos.x, pos.y, pos.z)
        quat = quat.rotate2D(location.yaw.toDouble(), location.x, location.z)
        particle.sendTo(
            adaptPlayer(player),
            taboolib.common.util.Location(location.world!!.name, quat.x(), quat.y(), quat.z()),
            Vector(offsetX, offsetY, offsetZ),
            count,
            speed,
            data,
        )
    }

    override fun toString(): String {
        return "Effect(source='$source', demand=$demand, particle=$particle, offsetX=$offsetX, offsetY=$offsetY, offsetZ=$offsetZ, posX=$posX, posY=$posY, posZ=$posZ, speed=$speed, count=$count, data=$data)"
    }
}