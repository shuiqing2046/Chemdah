package ink.ptms.chemdah.core.quest.objective.bukkit

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Task
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import taboolib.common.reflect.Reflex.Companion.invokeMethod

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IItemPickExp
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IItemPickExp : ObjectiveCountableI<PlayerPickupExperienceEvent>() {

    override val name = "pickup exp"
    override val event = PlayerPickupExperienceEvent::class.java

    init {
        handler {
            it.player
        }
        addSimpleCondition("position") { data, e ->
            data.toPosition().inside(e.player.location)
        }
        addSimpleCondition("reason") { data, e ->
            data.asList().any { it.equals(e.experienceOrb.invokeMethod<Any?>("getSpawnReason").toString(), true) }
        }
        addSimpleCondition("exp") { data, e ->
            data.toInt() <= e.experienceOrb.experience
        }
        addSimpleCondition("orb") { data, e ->
            data.toInferEntity().isEntity(e.experienceOrb)
        }
        addConditionVariable("exp") {
            it.experienceOrb.experience
        }
    }

    override fun getCount(profile: PlayerProfile, task: Task, event: PlayerPickupExperienceEvent): Int {
        return event.experienceOrb.experience
    }
}