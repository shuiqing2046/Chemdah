package ink.ptms.chemdah.core.quest.objective

import ink.ptms.chemdah.api.event.collect.ObjectiveEvents
import ink.ptms.chemdah.core.Data
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.AgentType
import ink.ptms.chemdah.core.quest.Quest
import ink.ptms.chemdah.core.quest.Task
import ink.ptms.chemdah.core.quest.addon.AddonRestart.Companion.canRestart
import ink.ptms.chemdah.util.*
import org.bukkit.entity.Player
import taboolib.common.platform.event.EventPriority
import taboolib.common5.mirrorFuture
import java.util.concurrent.CompletableFuture
import java.util.function.Function

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.Objective
 *
 * @author sky
 * @since 2021/3/1 11:52 下午
 */
@Suppress("UNCHECKED_CAST")
abstract class Objective<E : Any> {

    /**
     * 是否正在使用
     */
    var using = false

    /**
     * 条目继续的条件
     */
    internal val conditions = HashMap<String, Function3<PlayerProfile, Task, E, Boolean>>()

    /**
     * 在条目继续的条件中的额外脚本变量
     */
    internal val conditionVars = ArrayList<Function<E, Couple<String, Any>>>()

    /**
     * 条目完成的条件
     */
    internal val goals = HashMap<String, Function2<PlayerProfile, Task, Boolean>>()

    /**
     * 在条目完成的条件中的额外脚本变量
     */
    internal val goalVars = ArrayList<Function2<PlayerProfile, Task, Couple<String, Any>>>()

    /**
     * 条件序号
     */
    abstract val name: String

    /**
     * 事件类型
     */
    abstract val event: Class<E>

    /**
     * 事件优先级
     */
    open val priority = EventPriority.HIGHEST

    /**
     * 事件忽略已取消
     */
    open val ignoreCancelled = true

    /**
     * 是否注册事件
     */
    open val isListener = true

    /**
     * 是否异步执行
     */
    open val isAsync = false

    /**
     * 获取事件中的玩家
     */
    var handler: Function<E, Player?> = Function { null }
        private set

    /**
     * 获取事件中的玩家
     */
    fun handler(handle: Function<E, Player?>) {
        this.handler = handle
    }

    /**
     * 当条目继续时
     */
    internal open fun onContinue(profile: PlayerProfile, task: Task, quest: Quest, event: Any) {
    }

    /**
     * 当条目完成时
     */
    internal open fun onComplete(profile: PlayerProfile, task: Task, quest: Quest) {
        setCompletedSignature(profile, task, true)
    }

    /**
     * 当条目重置时
     */
    internal open fun onReset(profile: PlayerProfile, task: Task, quest: Quest) {
        profile.dataOperator(task) { clear() }
    }

    /**
     * 添加条目继续的条件
     * 简化版本
     */
    fun addSimpleCondition(name: String, func: Function2<Data, E, Boolean>) {
        conditions[name] = Function3 { _, task, e -> func(task.condition[name]!!, e) }
    }

    /**
     * 添加条目继续的条件
     * 完整版本
     */
    fun addFullCondition(name: String, func: Function3<PlayerProfile, Task, E, Boolean>) {
        conditions[name] = func
    }

    /**
     * 检查条目继续的所有条件
     * 当所有条件满足时再检查脚本代理
     */
    open fun checkCondition(profile: PlayerProfile, task: Task, quest: Quest, event: E): CompletableFuture<Boolean> {
        return if (conditions.all { (name, cond) -> !task.condition.containsKey(name) || cond(profile, task, event) }) {
            profile.checkAgent(task.condition["$"]?.data, quest, conditionVars.mapNotNull { safely { it.apply(event) } }.toMap())
        } else {
            CompletableFuture.completedFuture(false)
        }
    }

    /**
     * 添加条目完成的条件
     */
    fun addGoal(name: String, func: Function2<PlayerProfile, Task, Boolean>) {
        goals[name] = func
    }

    /**
     * 检查条目完成的所有条件
     * 当所有条件满足时再检查脚本代理
     *
     * 当 "completed" 为 "true" 则强制判定为已完成
     */
    open fun checkGoal(profile: PlayerProfile, quest: Quest, task: Task): CompletableFuture<Boolean> {
        return when {
            hasCompletedSignature(profile, task) -> CompletableFuture.completedFuture(false)
            goals.all { it.value(profile, task) } -> profile.checkAgent(task.goal["$"]?.data, quest)
            else -> CompletableFuture.completedFuture(false)
        }
    }

    /**
     * 检查条目完成的所有条件
     * 当条件满足时则完成条目
     *
     * 优先检测重置条件
     * 满足时则不会完成条目
     */
    open fun checkComplete(profile: PlayerProfile, task: Task, quest: Quest) {
        if (!hasCompletedSignature(profile, task)) {
            mirrorFuture<Int>("Objective:checkComplete") {
                task.canRestart(profile).thenAccept { r ->
                    if (r) {
                        if (ObjectiveEvents.Restart.Pre(this@Objective, task, quest, profile).call()) {
                            onReset(profile, task, quest)
                            task.agent(quest.profile, AgentType.TASK_RESTARTED)
                            ObjectiveEvents.Restart.Post(this@Objective, task, quest, profile).call()
                        }
                        finish(0)
                    } else {
                        checkGoal(profile, quest, task).thenAccept {
                            if (it && !hasCompletedSignature(profile, task)) {
                                if (ObjectiveEvents.Complete.Pre(this@Objective, task, quest, profile).call()) {
                                    onComplete(profile, task, quest)
                                    task.agent(quest.profile, AgentType.TASK_COMPLETED)
                                    ObjectiveEvents.Complete.Post(this@Objective, task, quest, profile).call()
                                }
                            }
                            finish(0)
                        }
                    }
                }
            }
        }
    }

    /**
     * 设置目标为已完成状态
     */
    open fun setCompletedSignature(profile: PlayerProfile, task: Task, value: Boolean) {
        profile.dataOperator(task) {
            put("completed", value)
        }
    }

    /**
     * 检测目标是否为已完成状态
     */
    open fun hasCompletedSignature(profile: PlayerProfile, task: Task): Boolean {
        return profile.dataOperator(task) {
            get("completed", false).toBoolean()
        }
    }

    /**
     * 获取条目进度
     */
    open fun getProgress(profile: PlayerProfile, task: Task): Progress {
        return Progress.empty
    }

    /**
     * 增加在条目继续的条件中的额外脚本变量
     */
    fun addConditionVariable(name: String, func: Function<E, Any>) {
        conditionVars += Function { Couple(name, func) }
    }

    /**
     * 增加在条目完成的条件中的额外脚本变量
     */
    fun addGoalVariable(name: String, func: Function2<PlayerProfile, Task, Any>) {
        goalVars += Function2 { profile, task -> Couple(name, func(profile, task)) }
    }
}