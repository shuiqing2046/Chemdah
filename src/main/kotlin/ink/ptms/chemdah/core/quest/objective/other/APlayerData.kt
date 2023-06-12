package ink.ptms.chemdah.core.quest.objective.other

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Task
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import ink.ptms.chemdah.core.quest.objective.Progress
import ink.ptms.chemdah.core.quest.objective.Progress.Companion.toProgress
import org.bukkit.event.Event
import taboolib.common5.Coerce
import taboolib.common5.cdouble

/**
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
abstract class APlayerData : ObjectiveCountableI<Event>() {

    override val event = Event::class.java
    override val isListener = false
    override val isTickable = true

    init {
        addGoal("key,value") { profile, task ->
            val data = getValue(profile, task, task.goal["key"].toString())
            val target = task.goal["value"].toString()
            // 如果目标为数字类型，则进行 gte 判断
            if (target.isNumber()) {
                data.toDouble() >= target.cdouble
            } else {
                data == target
            }
        }
    }

    override fun getProgress(profile: PlayerProfile, task: Task): Progress {
        val target = task.goal["value"].cdouble
        return if (hasCompletedSignature(profile, task)) {
            target.toProgress(target, 1.0)
        } else {
            getValue(profile, task, task.goal["key"].toString()).cdouble.toProgress(target)
        }
    }

    protected fun String.isNumber(): Boolean {
        return Coerce.asDouble(this) != null
    }

    /**
     * 数据获取接口
     */
    abstract fun getValue(profile: PlayerProfile, task: Task, key: String): String
}