package ink.ptms.chemdah.core

import ink.ptms.chemdah.core.quest.Idx
import ink.ptms.chemdah.core.quest.Quest
import ink.ptms.chemdah.core.quest.QuestDataOperator
import ink.ptms.chemdah.core.quest.Task
import ink.ptms.chemdah.core.quest.meta.MetaAlias.Companion.alias
import ink.ptms.chemdah.core.quest.meta.MetaLabel.Companion.label
import ink.ptms.chemdah.core.script.namespaceQuest
import ink.ptms.chemdah.core.script.print
import ink.ptms.chemdah.util.asList
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

    val player: Player
        get() = Bukkit.getPlayer(uniqueId)!!

    val quests: Collection<Quest>
        get() = questMap.filterValues { it.isValid }.values

    val persistentDataContainer = DataContainer()

    private val questMap = ConcurrentHashMap<String, Quest>()

    /**
     * 强制注册新的任务
     * 会覆盖原有的相同任务且不会进行任何条件判断和触发事件
     */
    fun registerQuest(quest: Quest) {
        questMap[quest.id] = quest
    }

    /**
     * 强制注销任务
     * 不会进行任何条件判断和触发事件
     */
    fun unregisterQuest(quest: Quest) {
        questMap.remove(quest.id)
    }

    /**
     * 获取条目数据控制器
     */
    fun <T> dataOperator(task: Task, func: QuestDataOperator.() -> T): T {
        return func.invoke(QuestDataOperator(this, task))
    }

    /**
     * 通过事件获取所有正在进行中的有效条目（有效任务）
     */
    fun getTasks(event: Event): List<Task> {
        return quests.flatMap { quest -> quest.tasks.filter { it.objective.event.isInstance(event) } }
    }

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

    fun checkAgent(agent: Any?, event: Event? = null): CompletableFuture<Boolean> {
        agent ?: return CompletableFuture.completedFuture(true)
        return try {
            KetherShell.eval(agent.asList(), namespace = namespaceQuest) {
                this.sender = player
                this.event = event
            }.thenApply {
                Coerce.toBoolean(it)
            }
        } catch (e: Throwable) {
            e.print()
            CompletableFuture.completedFuture(false)
        }
    }
}