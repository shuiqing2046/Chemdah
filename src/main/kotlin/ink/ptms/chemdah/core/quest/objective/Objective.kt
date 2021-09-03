package ink.ptms.chemdah.core.quest.objective

import ink.ptms.chemdah.api.event.collect.ObjectiveEvents
import ink.ptms.chemdah.core.Data
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.AgentType
import ink.ptms.chemdah.core.quest.Quest
import ink.ptms.chemdah.core.quest.Task
import ink.ptms.chemdah.core.quest.addon.AddonRestart.Companion.canRestart
import ink.ptms.chemdah.util.Function2
import ink.ptms.chemdah.util.Function3
import ink.ptms.chemdah.util.safely
import org.bukkit.entity.Player
import taboolib.common.platform.event.EventPriority
import taboolib.common5.mirrorFuture
import java.util.concurrent.CompletableFuture
import java.util.function.Function
import kotlin.reflect.KClass

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
    private val conditions = ArrayList<(PlayerProfile, Task, E) -> Boolean>()

    /**
     * 在条目继续的条件中的额外脚本变量
     */
    private val conditionVars = ArrayList<(E) -> Pair<String, Any>>()

    /**
     * 条目完成的条件
     */
    private val goals = ArrayList<(PlayerProfile, Task) -> Boolean>()

    /**
     * 在条目完成的条件中的额外脚本变量
     */
    private val goalVars = ArrayList<(PlayerProfile, Task) -> Pair<String, Any>>()

    /**
     * 条件序号
     */
    abstract val name: String

    /**
     * 事件类型
     */
    abstract val event: KClass<E>

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
    protected fun handler(handle: Function<E, Player?>) {
        this.handler = handle
    }

    /**
     * 内部方法
     */
    protected fun handler(handle: E.() -> Player?) {
        this.handler = Function { handle(it) }
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
        profile.dataOperator(task) {
            clear()
        }
    }

    /**
     * 添加条目继续的条件
     */
    fun addCondition(func: Function3<PlayerProfile, Task, E, Boolean>) {
        addCondition { playerProfile, task, e -> func(playerProfile, task, e) }
    }

    /**
     * 内部接口
     */
    internal fun addCondition(func: (PlayerProfile, Task, E) -> Boolean) {
        conditions += func
    }

    /**
     * 添加条目继续的条件
     * 简化版本
     */
    fun addCondition(name: String, func: Function2<Data, E, Boolean>) {
        addCondition(name) { event ->
            func(this, event)
        }
    }

    /**
     * 内部接口
     */
    internal fun addCondition(name: String, func: Data.(E) -> Boolean) {
        conditions += { _, task, e ->
            !task.condition.containsKey(name) || func(task.condition[name]!!, e)
        }
    }

    /**
     * 检查条目继续的所有条件
     * 当所有条件满足时再检查脚本代理
     */
    open fun checkCondition(profile: PlayerProfile, task: Task, event: E): CompletableFuture<Boolean> {
        return if (conditions.all { it(profile, task, event) }) {
            profile.checkAgent(task.condition["$"]?.data, event, conditionVars.mapNotNull { safely { it(event) } }.toMap())
        } else {
            CompletableFuture.completedFuture(false)
        }
    }

    /**
     * 添加条目完成的条件
     */
    fun addGoal(func: Function2<PlayerProfile, Task, Boolean>) {
        addGoal { profile, task ->
            func(profile, task)
        }
    }

    /**
     * 内部接口
     */
    internal fun addGoal(func: (PlayerProfile, Task) -> Boolean) {
        goals += func
    }

    /**
     * 检查条目完成的所有条件
     * 当所有条件满足时再检查脚本代理
     *
     * 当 "completed" 为 "true" 则强制判定为已完成
     */
    open fun checkGoal(profile: PlayerProfile, task: Task): CompletableFuture<Boolean> {
        return when {
            hasCompletedSignature(profile, task) -> CompletableFuture.completedFuture(false)
            goals.all { it(profile, task) } -> profile.checkAgent(task.goal["$"]?.data)
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
                        checkGoal(profile, task).thenAccept {
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
        addConditionVariable(name) {
            func.apply(it)
        }
    }

    /**
     * 内部接口
     */
    internal fun addConditionVariable(name: String, func: (E) -> Any) {
        conditionVars += { name to func(it) }
    }

    /**
     * 增加在条目完成的条件中的额外脚本变量
     */
    fun addGoalVariable(name: String, func: Function2<PlayerProfile, Task, Any>) {
        addGoalVariable(name) { playerProfile, task ->
            func(playerProfile, task)
        }
    }

    /**
     * 内部接口
     */
    internal fun addGoalVariable(name: String, func: (PlayerProfile, Task) -> Any) {
        goalVars += { a, b -> name to func(a, b) }
    }
}