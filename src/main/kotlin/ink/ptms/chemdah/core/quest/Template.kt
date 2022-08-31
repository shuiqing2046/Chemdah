package ink.ptms.chemdah.core.quest

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.api.event.collect.QuestEvents
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.addon.AddonControl.Companion.control
import ink.ptms.chemdah.core.quest.addon.data.ControlTrigger
import ink.ptms.chemdah.core.quest.meta.Meta
import taboolib.common.util.asList
import taboolib.library.configuration.ConfigurationSection
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.Template
 *
 * @author sky
 * @since 2021/3/1 11:43 下午
 */
class Template(id: String, config: ConfigurationSection) : QuestContainer(id, config) {

    /**
     * 所有任务条目
     */
    val taskMap = HashMap<String, Task>()

    /**
     * 元数据引用
     */
    private val metaImport = config["meta.import"]?.asList() ?: emptyList()

    init {
        config.getKeys(false).filter { it.startsWith("task:") }.forEach { loadTask(it.substring("task:".length), it) }
        config.getConfigurationSection("task")?.getKeys(false)?.forEach { node -> loadTask(node) }
    }

    /**
     * 获取包含模板导入 (import) 的所有任务元数据
     * 已配置的元数据会覆盖导入源
     */
    fun metaAll(): Map<String, Meta<*>> {
        return HashMap<String, Meta<*>>().also {
            if (metaImport.isNotEmpty()) {
                metaImport.forEach { clone ->
                    ChemdahAPI.getQuestTemplate(clone)?.metaAll()?.run { it.putAll(this) }
                }
            }
            it.putAll(metaMap)
        }
    }

    /**
     * 使玩家接受任务
     */
    fun acceptTo(profile: PlayerProfile): CompletableFuture<AcceptResult> {
        return checkAccept(profile).thenApply {
            if (it.type == AcceptResult.Type.SUCCESSFUL) {
                val quest = Quest(id, profile)
                val control = control()
                control.signature(profile, ControlTrigger.ACCEPT)
                profile.registerQuest(quest)
                agent(profile, AgentType.QUEST_ACCEPTED)
                QuestEvents.Accept.Post(quest, profile).call()
            } else {
                agent(profile, AgentType.QUEST_ACCEPT_CANCELLED, reason = it.reason)
            }
            it
        }
    }

    /**
     * 检测玩家是否可以接受该任务
     */
    fun checkAccept(profile: PlayerProfile): CompletableFuture<AcceptResult> {
        val future = CompletableFuture<AcceptResult>()
        if (profile.getQuestById(id, openAPI = false) != null) {
            future.complete(AcceptResult(AcceptResult.Type.ALREADY_EXISTS))
            return future
        }
        val pre = QuestEvents.Accept.Pre(this@Template, profile)
        pre.call()
        if (pre.isCancelled) {
            future.complete(AcceptResult(AcceptResult.Type.CANCELLED_BY_EVENT, pre.reason))
            return future
        }
        val control = control()
        control.check(profile).thenApply { c ->
            if (c.pass) {
                agent(profile, AgentType.QUEST_ACCEPT).thenAccept { a ->
                    if (a) {
                        future.complete(AcceptResult(AcceptResult.Type.SUCCESSFUL))
                    } else {
                        future.complete(AcceptResult(AcceptResult.Type.CANCELLED_BY_AGENT))
                    }
                }
            } else {
                future.complete(AcceptResult(AcceptResult.Type.CANCELLED_BY_CONTROL, c.reason))
            }
        }
        return future
    }

    private fun loadTask(taskId: String, taskNode: String = "task.$taskId") {
        taskMap[taskId] = Task(taskId, config.getConfigurationSection(taskNode)!!, this)
    }
}