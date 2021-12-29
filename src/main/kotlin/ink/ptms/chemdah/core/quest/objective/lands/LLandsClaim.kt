package ink.ptms.chemdah.core.quest.objective.lands

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import me.angeschossen.lands.api.events.ChunkPostClaimEvent

@Dependency("Lands")
object LLandsClaim : ObjectiveCountableI<ChunkPostClaimEvent>() {

    override val name = "lands claim"
    override val event = ChunkPostClaimEvent::class.java

    init {
        handler {
            it.landPlayer.player
        }
        addSimpleCondition("position") { data, it ->
            data.toPosition().inside(it.landPlayer.player.location)
        }
    }
}