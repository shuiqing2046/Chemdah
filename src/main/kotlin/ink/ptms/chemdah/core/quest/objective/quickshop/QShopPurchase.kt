package ink.ptms.chemdah.core.quest.objective.quickshop

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.maxgamer.quickshop.event.ShopPurchaseEvent

@Dependency("QuickShop")
object QShopPurchase : ObjectiveCountableI<ShopPurchaseEvent>() {

    override val name = "quickshop purchase"
    override val event = ShopPurchaseEvent::class

    init {
        handler {
            player
        }
        addSimpleCondition("position") {
            toPosition().inside(it.player!!.location)
        }
    }
}