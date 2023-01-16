package ink.ptms.chemdah.core

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.api.event.collect.QuestEvents
import ink.ptms.chemdah.core.database.Database
import ink.ptms.chemdah.core.quest.Quest
import ink.ptms.chemdah.core.quest.QuestDataOperator
import ink.ptms.chemdah.core.quest.Task
import ink.ptms.chemdah.core.quest.Template
import ink.ptms.chemdah.core.quest.objective.Objective
import ink.ptms.chemdah.util.Couple
import ink.ptms.chemdah.util.namespaceQuest
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import taboolib.common.platform.function.adaptCommandSender
import taboolib.common.platform.function.submitAsync
import taboolib.common.util.asList
import taboolib.common5.Coerce
import taboolib.module.kether.KetherShell
import taboolib.module.kether.printKetherErrorMessage
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer
import java.util.function.Function

/**
 * Chemdah
 * ink.ptms.chemdah.core.PlayerProfile
 *
 * @author sky
 * @since 2021/3/2 12:00 上午
 */
class PlayerProfile(val uniqueId: UUID) {

    /**
     * 玩家实例
     */
    val player: Player
        get() = Bukkit.getPlayer(uniqueId)!!

    /**
     * 玩家是否在线
     */
    val isPlayerOnline: Boolean
        get() = Bukkit.getPlayer(uniqueId) != null

    /**
     * 任务或玩家数据是否发生变动
     */
    val isDataChanged: Boolean
        get() = persistentDataContainer.isChanged || getQuests().any { it.newQuest || it.persistentDataContainer.isChanged }

    /**
     * 持久化数据储存容器
     */
    val persistentDataContainer = DataContainer()

    /**
     * 任务容器
     */
    private val questMap = ConcurrentHashMap<String, Quest>()

    /**
     * 强制注册新的任务
     * 会覆盖原有的相同任务且不会进行任何条件判断和触发事件
     */
    fun registerQuest(quest: Quest, newQuest: Boolean = true) {
        questMap[quest.id] = quest
        if (quest.isValid) {
            quest.newQuest = newQuest
            QuestEvents.Registered(quest, this).call()
        }
    }

    /**
     * 强制注销任务
     * 不会进行任何条件判断和触发事件
     * @param release 是否从数据库释放数据
     */
    fun unregisterQuest(quest: Quest, release: Boolean = true) {
        // 删除缓存
        questMap.remove(quest.id)
        // 释放数据
        if (release) {
            submitAsync { Database.INSTANCE.releaseQuest(player, this@PlayerProfile, quest) }
        }
        QuestEvents.Unregistered(quest, this).call()
    }

    internal fun <T> dataOperator(task: Task, func: QuestDataOperator.() -> T): T {
        return func.invoke(QuestDataOperator(this, task))
    }

    /**
     * 获取条目数据控制器
     */
    fun <T> dataOperator(task: Task, func: Function<QuestDataOperator, T>): T {
        return func.apply(QuestDataOperator(this, task))
    }

    /**
     * 通过序号获取正在进行中的有效任务
     */
    fun getQuestById(value: String, openAPI: Boolean = true): Quest? {
        return if (openAPI) {
            getQuests(true).firstOrNull { it.id == value }
        } else {
            questMap[value]?.takeIf { it.isValid }
        }
    }

    /**
     * 获取所有正在进行中的有效任务
     * @param openAPI 是否启用开放 API，即允许第三方直接修改这个结果
     */
    fun getQuests(openAPI: Boolean = false): List<Quest> {
        return if (openAPI) {
            ChemdahAPI.eventFactory.callQuestCollect(this, questMap.values.filter { it.isValid })
        } else {
            questMap.values.filter { it.isValid }
        }
    }

    /**
     * 通过序号判断该任务是否已经完成
     */
    fun isQuestCompleted(value: String): Boolean {
        return getQuestCompletedDate(value) > 0L
    }

    /**
     * 通过模板判断该任务是否已经完成
     */
    fun isQuestCompleted(template: Template): Boolean {
        return isQuestCompleted(template.id)
    }

    /**
     * 通过序号判断该任务的上次完成时间
     */
    fun getQuestCompletedDate(value: String): Long {
        return persistentDataContainer["quest.complete.$value", 0L].toLong()
    }

    /**
     * 通过模板判断该任务的上次完成时间
     */
    fun getQuestCompletedDate(template: Template): Long {
        return getQuestCompletedDate(template.id)
    }

    /**
     * 执行事件脚本代理
     */
    fun checkAgent(agent: Any?, quest: Quest? = null, variables: Map<String, Any> = emptyMap()): CompletableFuture<Boolean> {
        agent ?: return CompletableFuture.completedFuture(true)
        return try {
            KetherShell.eval(agent.asList(), sender = adaptCommandSender(player), namespace = namespaceQuest) {
                set("@QuestSelected", quest?.template?.node)
                set("@QuestContainer", quest)
                variables.forEach { (t, u) -> set(t, u) }
            }.thenApply {
                Coerce.toBoolean(it)
            }
        } catch (e: Throwable) {
            e.printKetherErrorMessage()
            CompletableFuture.completedFuture(false)
        }
    }

    /**
     * 通过事件获取所有正在进行中的有效条目（有效任务）
     */
    fun tasks(event: Any, func: Consumer<Couple<Quest, Task>>) {
        getQuests(true).forEach { quest ->
            quest.tasks.filter { it.objective.isListener && it.objective.event.isInstance(event) }.forEach { func.accept(Couple(quest, it)) }
        }
    }

    /**
     * 通过 Objective 获取所有正在进行中的有效条目（有效任务）
     */
    fun tasks(objective: Objective<*>, func: Consumer<Couple<Quest, Task>>) {
        getQuests(true).forEach { quest -> quest.tasks.filter { it.objective == objective }.forEach { func.accept(Couple(quest, it)) } }
    }

    /**
     * 检查并更新数据
     */
    fun push() {
        Database.INSTANCE.update(player, this)
    }
}