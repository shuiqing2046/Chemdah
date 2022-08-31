package ink.ptms.chemdah.core.quest.objective.other

import ink.ptms.chemdah.api.event.collect.PlayerEvents
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Task
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import ink.ptms.chemdah.core.quest.objective.Progress
import ink.ptms.chemdah.core.quest.objective.Progress.Companion.toProgress
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
object ICustomLevel : ObjectiveCountableI<PlayerEvents.LevelChange>() {

    override val name = "custom level"
    override val event = PlayerEvents.LevelChange::class.java
    override val isListener = true

    init {
        handler {
            it.player
        }
        addGoal("id,level") { profile, task ->
            val option = LevelSystem.getLevelOption(task.goal["id"].toString()) ?: return@addGoal false
            profile.getLevel(option).level >= task.goal["level", 1].toInt()
        }
        addGoalVariable("level") { profile, task ->
            val option = LevelSystem.getLevelOption(task.goal["id"].toString()) ?: return@addGoalVariable -1
            profile.getLevel(option).level
        }
    }

    override fun getProgress(profile: PlayerProfile, task: Task): Progress {
        val option = LevelSystem.getLevelOption(task.goal["id"].toString()) ?: return Progress.ZERO
        val target = task.goal["level", 1].toInt()
        return if (hasCompletedSignature(profile, task)) {
            target.toProgress(target, 1.0)
        } else {
            profile.getLevel(option).level.toProgress(target)
        }
    }
}