package ink.ptms.chemdah.api

import ink.ptms.adyeshach.api.AdyeshachAPI
import ink.ptms.adyeshach.common.entity.EntityTypes
import ink.ptms.adyeshach.common.entity.type.AdyArmorStand
import io.izzel.taboolib.module.hologram.THologram
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.*

object HologramAPI {

    abstract class Hologram<T> {

        private val map = HashMap<Int, T>()

        fun create(player: Player, location: Location, content: List<String>) {
            content.forEachIndexed { index, line ->
                map[index] = create(player, location.clone().add(0.0, (((content.size - 1) - index) * 0.3), 0.0), line)
            }
        }

        fun teleport(location: Location) {
            map.forEach { (index, obj) ->
                teleport(obj, location.clone().add(0.0, (((map.size - 1) - index) * 0.3), 0.0))
            }
        }

        fun edit(content: List<String>) {
            content.forEachIndexed { index, line ->
                if (index < map.size) {
                    edit(map[index]!!, line)
                }
            }
        }

        fun delete() {
            map.forEach { delete(it.value) }
        }

        protected abstract fun create(player: Player, location: Location, line: String): T

        protected abstract fun edit(obj: T, line: String)

        protected abstract fun teleport(obj: T, location: Location)

        protected abstract fun delete(obj: T)
    }

    class HologramAdyeshach : Hologram<AdyArmorStand>() {

        override fun create(player: Player, location: Location, line: String): AdyArmorStand {
            return AdyeshachAPI.getEntityManagerPrivateTemporary(player).create(EntityTypes.ARMOR_STAND, location) {
                val npc = it as AdyArmorStand
                npc.setSmall(true)
                npc.setMarker(true)
                npc.setBasePlate(false)
                npc.setInvisible(true)
                npc.setCustomName(line)
                npc.setCustomNameVisible(true)
            } as AdyArmorStand
        }

        override fun edit(obj: AdyArmorStand, line: String) {
            obj.setCustomName(line)
        }

        override fun teleport(obj: AdyArmorStand, location: Location) {
            obj.teleport(location)
        }

        override fun delete(obj: AdyArmorStand) {
            obj.delete()
        }
    }

    class HologramNative : Hologram<io.izzel.taboolib.module.hologram.Hologram>() {

        override fun create(player: Player, location: Location, line: String): io.izzel.taboolib.module.hologram.Hologram {
            return THologram.create(location, line).also {
                it.addViewer(player)
            }
        }

        override fun edit(obj: io.izzel.taboolib.module.hologram.Hologram, line: String) {
            obj.flash(line)
        }

        override fun teleport(obj: io.izzel.taboolib.module.hologram.Hologram, location: Location) {
            obj.flash(location)
        }

        override fun delete(obj: io.izzel.taboolib.module.hologram.Hologram) {
            obj.delete()
        }
    }

    val isAdyeshachHooked by lazy {
        Bukkit.getPluginManager().getPlugin("Adyeshach") != null
    }

    /**
     * 创建全息
     */
    fun Player.createHologram(location: Location, content: List<String>): Hologram<*> {
        return if (isAdyeshachHooked) {
            HologramAdyeshach().also {
                it.create(this, location, content)
            }
        } else {
            HologramNative().also {
                it.create(this, location, content)
            }
        }
    }
}