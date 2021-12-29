package ink.ptms.chemdah.core.quest.objective.marriagemaster

import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.GiftEvent
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI

@Dependency("MarriageMaster")
object MMarriageGift : ObjectiveCountableI<GiftEvent>() {

    override val name = "marriage gift"
    override val event = GiftEvent::class.java

    init {
        handler {
            player.playerOnline
        }
        addSimpleCondition("position") {
            toPosition().inside(it.player.playerOnline!!.location)
        }
        addSimpleCondition("item") {
            toInferItem().isItem(it.itemStack)
        }
    }
}