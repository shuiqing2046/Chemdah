package ink.ptms.chemdah.core.quest.objective.quickshop

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.Bukkit
import org.maxgamer.quickshop.event.ShopDeleteEvent

@Dependency("QuickShop")
object QShopDelete : ObjectiveCountableI<ShopDeleteEvent>() {

    override val name = "quickshop delete"
    override val event = ShopDeleteEvent::class.java

    init {
        handler {
            Bukkit.getPlayer(it.shop.owner)
        }
        addSimpleCondition("position") { data, it ->
            data.toPosition().inside(Bukkit.getPlayer(it.shop.owner)!!.location)
        }
    }
}