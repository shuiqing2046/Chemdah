package ink.ptms.chemdah.core.quest.objective.other

import ink.ptms.chemdah.api.event.PlayerEvent
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountable
import ink.ptms.chemdah.module.level.LevelSystem
import ink.ptms.chemdah.module.level.LevelSystem.getLevel

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.other.ICustomLevel
 *
 * task:0:
 *   objective: custom level
 *   goal:
 *      id: def
 *      level: 10
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object ICustomLevel : ObjectiveCountable<PlayerEvent.LevelChange>() {

    override val name = "custom level"
    override val event = PlayerEvent.LevelChange::class
    override val isListener = true

    init {
        addGoal { profile, task ->
            val option = LevelSystem.getLevelOption(task.goal["id"].toString()) ?: return@addGoal false
            profile.getLevel(option).level >= task.goal["level", 1].toInt()
        }
        addGoalVariable("level") { profile, task ->
            val option = LevelSystem.getLevelOption(task.goal["id"].toString()) ?: return@addGoalVariable -1
            profile.getLevel(option).level
        }
    }
}