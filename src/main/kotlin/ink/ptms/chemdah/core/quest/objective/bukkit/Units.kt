package ink.ptms.chemdah.core.quest.objective.bukkit

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.inventory.ItemStack

val AIR = ItemStack(Material.AIR)

val EMPTY = Location(Bukkit.getWorlds()[0], 0.0, 0.0, 0.0)

val EMPTY_EVENT = object : Event() {

    override fun getHandlers(): HandlerList {
        TODO("Not yet implemented")
    }
}
