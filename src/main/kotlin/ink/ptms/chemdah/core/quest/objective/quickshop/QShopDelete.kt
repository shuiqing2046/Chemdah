package ink.ptms.chemdah.core.quest.objective.quickshop

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.Bukkit
import org.maxgamer.quickshop.event.ShopDeleteEvent

@Dependency("QuickShop")
object QShopDelete : ObjectiveCountableI<ShopDeleteEvent>() {

    override val name = "quickshop delete"
    override val event = ShopDeleteEvent::class

    init {
        handler {
            if (shop != null) Bukkit.getPlayer(shop.owner) else null
        }
        addSimpleCondition("position") {
            toPosition().inside(Bukkit.getPlayer(it.shop.owner)!!.location)
        }
    }
}