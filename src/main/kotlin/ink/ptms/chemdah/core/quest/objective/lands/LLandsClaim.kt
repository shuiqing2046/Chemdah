package ink.ptms.chemdah.core.quest.objective.lands

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import me.angeschossen.lands.api.events.ChunkPostClaimEvent

@Dependency("Lands")
object LLandsClaim : ObjectiveCountableI<ChunkPostClaimEvent>() {

    override val name = "lands claim"
    override val event = ChunkPostClaimEvent::class

    init {
        handler {
            landPlayer.player
        }
        addSimpleCondition("position") {
            toPosition().inside(it.landPlayer.player.location)
        }
    }
}