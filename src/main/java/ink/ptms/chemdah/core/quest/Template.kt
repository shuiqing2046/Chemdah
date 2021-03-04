package ink.ptms.chemdah.core.quest

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.api.event.QuestEvents
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.meta.Meta
import ink.ptms.chemdah.core.quest.meta.MetaControl.Companion.control
import ink.ptms.chemdah.util.asList
import ink.ptms.chemdah.util.mirrorDefine
import ink.ptms.chemdah.util.mirrorFinish
import io.netty.util.concurrent.CompleteFuture
import org.bukkit.configuration.ConfigurationSection
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.Template
 *
 * @author sky
 * @since 2021/3/1 11:43 下午
 */
class Template(val id: String, config: ConfigurationSection) : QuestContainer(config) {

    val task = HashMap<String, Task>()
    val metaImport = config.get("meta.import")?.asList() ?: emptyList()

    init {
        // 加载条目
        config.getKeys(false)
            .filter { it.startsWith("task(") && it.endsWith(")") }
            .forEach {
                val taskId = it.substring("task(".length, it.length - 1)
                task[taskId] = Task(taskId, config.getConfigurationSection(it)!!, this)
            }
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
            it.putAll(meta)
        }
    }

    /**
     * 使玩家接受任务
     */
    fun acceptTo(profile: PlayerProfile): CompletableFuture<AcceptResult> {
        mirrorDefine("Template:acceptTo")
        val future = CompletableFuture<AcceptResult>()
        if (profile.questValid.containsKey(id)) {
            future.complete(AcceptResult.ALREADY_EXISTS)
            mirrorFinish("Template:acceptTo")
            return future
        }
        if (QuestEvents.Accept(this, profile).call().isCancelled) {
            future.complete(AcceptResult.CANCELLED_BY_EVENT)
            mirrorFinish("Template:acceptTo")
            return future
        }
        val control = control()
        if (control.check(profile)) {
            agent(profile, AgentType.QUEST_ACCEPT).thenAccept {
                if (it) {
                    control.signature(profile)
                    profile.quest[id] = Quest(id, profile)
                    future.complete(AcceptResult.SUCCESSFUL)
                } else {
                    future.complete(AcceptResult.CANCELLED_BY_AGENT)
                }
            }
        } else {
            future.complete(AcceptResult.CANCELLED_BY_CONTROL)
        }
        mirrorFinish("Template:acceptTo")
        return future
    }
}