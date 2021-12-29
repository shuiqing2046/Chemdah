package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Task
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.event.player.PlayerLevelChangeEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerLevelChange
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerLevelChange : ObjectiveCountableI<PlayerLevelChangeEvent>() {

    override val name = "level change"
    override val event = PlayerLevelChangeEvent::class.java

    init {
        handler {
            it.player
        }
        addSimpleCondition("position") { data, e ->
            data.toPosition().inside(e.player.location)
        }
        addSimpleCondition("level") { data, e ->
            data.toInt() <= e.newLevel - e.oldLevel
        }
        addSimpleCondition("level:new") { data, e ->
            data.toInt() <= e.newLevel
        }
        addSimpleCondition("level:old") { data, e ->
            data.toInt() <= e.oldLevel
        }
        addConditionVariable("level") { e ->
            e.newLevel - e.oldLevel
        }
        addConditionVariable("level:new") { e ->
            e.newLevel
        }
        addConditionVariable("level:old") { e ->
            e.oldLevel
        }
    }

    override fun getCount(profile: PlayerProfile, task: Task, event: PlayerLevelChangeEvent): Int {
        return event.newLevel - event.oldLevel
    }
}