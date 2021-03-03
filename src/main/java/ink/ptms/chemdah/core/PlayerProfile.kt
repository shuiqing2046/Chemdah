package ink.ptms.chemdah.core

import ink.ptms.chemdah.core.quest.Quest
import ink.ptms.chemdah.core.quest.QuestDataOperator
import ink.ptms.chemdah.core.quest.Task
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
import kotlin.collections.ArrayList

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

    val quest = ConcurrentHashMap<String, Quest>()
    val persistentDataContainer = DataContainer()

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
        return quest.filterValues { it.isValid }.flatMap { (_, v) ->
            v.template.task.values.filter { it.objective.event.isInstance(event) }
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