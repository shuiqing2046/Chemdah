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
            it.player.playerOnline
        }
        addSimpleCondition("position") { data, it ->
            data.toPosition().inside(it.player.playerOnline!!.location)
        }
        addSimpleCondition("item") { data, it ->
            data.toInferItem().isItem(it.itemStack)
        }
    }
}