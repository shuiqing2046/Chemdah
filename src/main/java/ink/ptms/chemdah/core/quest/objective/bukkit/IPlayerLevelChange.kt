package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Task
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountable
import org.bukkit.event.player.PlayerLevelChangeEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerLevelChange
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerLevelChange : ObjectiveCountable<PlayerLevelChangeEvent>() {

    override val name = "level change"
    override val event = PlayerLevelChangeEvent::class

    init {
        handler {
            player
        }
        addCondition("position") { e ->
            toPosition().inside(e.player.location)
        }
        addCondition("level") { e ->
            toInt() <= e.newLevel - e.oldLevel
        }
        addCondition("level:new") { e ->
            toInt() <= e.newLevel
        }
        addCondition("level:old") { e ->
            toInt() <= e.oldLevel
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