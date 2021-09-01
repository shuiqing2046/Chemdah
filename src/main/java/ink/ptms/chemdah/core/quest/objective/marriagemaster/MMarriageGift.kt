package ink.ptms.chemdah.core.quest.objective.marriagemaster

import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.GiftEvent
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI

@Dependency("MarriageMaster")
object MMarriageGift : ObjectiveCountableI<GiftEvent>() {

    override val name = "marriage gift"
    override val event = GiftEvent::class

    init {
        handler {
            player.playerOnline
        }
        addCondition("position") {
            toPosition().inside(it.player.playerOnline!!.location)
        }
        addCondition("item") {
            toInferItem().isItem(it.itemStack)
        }
    }
}