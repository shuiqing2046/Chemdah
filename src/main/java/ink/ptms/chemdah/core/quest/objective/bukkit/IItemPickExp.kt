package ink.ptms.chemdah.core.quest.objective.bukkit

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Task
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountable
import org.bukkit.event.player.PlayerPickupArrowEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IItemPickExp
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IItemPickExp : ObjectiveCountable<PlayerPickupExperienceEvent>() {

    override val name = "pickup exp"
    override val event = PlayerPickupExperienceEvent::class

    init {
        handler {
            player
        }
        addCondition("position") { e ->
            toPosition().inside(e.player.location)
        }
        addCondition("reason") { e ->
            asList().any { it.equals(e.experienceOrb.spawnReason.name, true) }
        }
        addCondition("exp") { e ->
            toInt() <= e.experienceOrb.experience
        }
        addCondition("orb") { e ->
            toInferEntity().isEntity(e.experienceOrb)
        }
        addConditionVariable("exp") {
            it.experienceOrb.experience
        }
    }

    override fun getCount(profile: PlayerProfile, task: Task, event: PlayerPickupExperienceEvent): Int {
        return event.experienceOrb.experience
    }
}