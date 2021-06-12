package ink.ptms.chemdah.util

import com.google.common.base.Enums
import io.izzel.taboolib.kotlin.Demand
import io.izzel.taboolib.kotlin.Reflex.Companion.reflexInvoke
import io.izzel.taboolib.util.Coerce
import io.izzel.taboolib.util.Quat
import io.izzel.taboolib.util.item.Items
import io.izzel.taboolib.util.lite.Effects
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * ClayParticle
 * com.mineclay.clayparticle.compat.ornament.ActionParticle
 *
 * @author bkm016
 * @since 2020/11/20 7:34 下午
 */
open class Effect(val source: String) {

    val demand = Demand(source)
    val particle = Enums.getIfPresent(Particle::class.java, demand.namespace.toUpperCase()).or(Particle.FLAME)!!
    val offsetX = Coerce.toDouble(demand.get(0, "0")!!)
    val offsetY = Coerce.toDouble(demand.get(1, "0")!!)
    val offsetZ = Coerce.toDouble(demand.get(2, "0")!!)
    val posX = Coerce.toDouble(demand.get("posX"))
    val posY = Coerce.toDouble(demand.get("posY"))
    val posZ = Coerce.toDouble(demand.get("posZ"))
    val speed = Coerce.toDouble(demand.get(listOf("speed", "s"), "0")!!)
    val count = Coerce.toInteger(demand.get(listOf("count", "c"), "1")!!)
    var data: Any? = null

    init {
        demand.get("block")?.let {
            data = (Items.asMaterial(it) ?: Material.STONE).reflexInvoke("createBlockData")
        }
        demand.get("item")?.let {
            data = ItemStack(Items.asMaterial(it.split(":")[0]) ?: Material.STONE, 1, Coerce.toShort(it.split(":").getOrElse(1) { "0" }))
        }
        demand.get("material")?.let {
            data = ItemStack(Items.asMaterial(it.split(":")[0]) ?: Material.STONE, 1, Coerce.toShort(it.split(":").getOrElse(1) { "0" })).data
        }
        demand.get("color")?.let {
            val color = it.split("~")[0].split("-")
            data = Effects.ColorData(
                Color.fromRGB(
                    Coerce.toInteger(color.getOrElse(0) { "0" }),
                    Coerce.toInteger(color.getOrElse(1) { "1" }),
                    Coerce.toInteger(color.getOrElse(2) { "2" })
                ),
                Coerce.toFloat(it.split("~").getOrElse(1) { "0" })
            )
        }
    }

    fun run(location: Location) {
        val quat = Quat.at(location.clone().add(posX, posY, posZ))
        Effects.create(particle, quat.rotate2D(location.yaw.toDouble(), location.x, location.z).toLocation(location.world))
            .offset(doubleArrayOf(offsetX, offsetY, offsetZ))
            .count(count)
            .speed(speed)
            .range(100.0)
            .data0(data)
            .playAsync()
    }

    fun run(location: Location, player: Player) {
        val quat = Quat.at(location.clone().add(posX, posY, posZ))
        Effects.create(particle, quat.rotate2D(location.yaw.toDouble(), location.x, location.z).toLocation(location.world))
            .offset(doubleArrayOf(offsetX, offsetY, offsetZ))
            .count(count)
            .speed(speed)
            .player(player)
            .data0(data)
            .playAsync()
    }

    override fun toString(): String {
        return "Effect(source='$source', demand=$demand, particle=$particle, offsetX=$offsetX, offsetY=$offsetY, offsetZ=$offsetZ, posX=$posX, posY=$posY, posZ=$posZ, speed=$speed, count=$count, data=$data)"
    }
}