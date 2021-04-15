package ink.ptms.chemdah.core

import ink.ptms.chemdah.api.event.QuestEvent
import ink.ptms.chemdah.core.database.Database
import ink.ptms.chemdah.core.quest.*
import ink.ptms.chemdah.core.quest.meta.MetaAlias.Companion.alias
import ink.ptms.chemdah.core.quest.meta.MetaLabel.Companion.label
import ink.ptms.chemdah.util.asList
import ink.ptms.chemdah.util.namespaceQuest
import ink.ptms.chemdah.util.print
import io.izzel.taboolib.kotlin.Tasks
import io.izzel.taboolib.kotlin.kether.KetherShell
import io.izzel.taboolib.util.Coerce
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Event
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

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
    val playerOnline: Boolean
        get() = Bukkit.getPlayer(uniqueId) != null

    /**
     * 所有正在进行中的有效任务
     */
    val quests: Collection<Quest>
        get() = questMap.filterValues { it.isValid }.values

    /**
     * 任务或玩家数据是否发生变动
     */
    val changed: Boolean
        get() = persistentDataContainer.changed || quests.any { it.persistentDataContainer.changed }

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
    fun registerQuest(quest: Quest) {
        questMap[quest.id] = quest
        if (quest.isValid) {
            quest.persistentDataContainer.changed = true
            QuestEvent.Registered(quest, this).call()
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
            Tasks.task(true) {
                Database.INSTANCE.releaseQuest(player, this, quest)
            }
        }
        QuestEvent.Unregistered(quest, this).call()
    }

    /**
     * 获取条目数据控制器
     */
    fun <T> dataOperator(task: Task, func: QuestDataOperator.() -> T) = func.invoke(QuestDataOperator(this, task))

    /**
     * 通过事件获取所有正在进行中的有效条目（有效任务）
     */
    fun getTasks(event: Event) = quests.flatMap { quest -> quest.tasks.filter { it.objective.isListener && it.objective.event.isInstance(event) } }

    /**
     * 通过序号获取正在进行中的有效任务
     */
    fun getQuestById(value: String) = getQuests(value, Idx.ID).firstOrNull()

    /**
     * 通过序号、别名或标签获取所有符合要求且正在进行中的有效任务
     */
    fun getQuests(value: String, idx: Idx = Idx.ID): List<Quest> {
        return when (idx) {
            Idx.ID -> {
                quests.filter { it.id == value }
            }
            Idx.ID_ALIAS -> {
                quests.filter { it.id == value || it.template.alias() == value }
            }
            Idx.LABEL -> {
                quests.filter { value in it.template.label() }
            }
        }
    }

    /**
     * 通过序号判断该任务是否已经完成
     */
    fun isQuestCompleted(value: String) = getQuestCompletedDate(value) > 0L

    /**
     * 通过模板判断该任务是否已经完成
     */
    fun isQuestCompleted(template: Template) = isQuestCompleted(template.id)

    /**
     * 通过序号判断该任务的上次完成时间
     */
    fun getQuestCompletedDate(value: String) = persistentDataContainer["quest.complete.$value", 0L].toLong()

    /**
     * 通过模板判断该任务的上次完成时间
     */
    fun getQuestCompletedDate(template: Template) = getQuestCompletedDate(template.id)

    /**
     * 执行事件脚本代理
     */
    fun checkAgent(agent: Any?, event: Event? = null, variables: Map<String, Any> = emptyMap()): CompletableFuture<Boolean> {
        agent ?: return CompletableFuture.completedFuture(true)
        return try {
            KetherShell.eval(agent.asList(), namespace = namespaceQuest) {
                this.sender = player
                this.event = event
                rootFrame().variables().also {
                    variables.forEach { (t, u) -> it.set(t, u) }
                }
            }.thenApply {
                Coerce.toBoolean(it)
            }
        } catch (e: Throwable) {
            e.print()
            CompletableFuture.completedFuture(false)
        }
    }
}