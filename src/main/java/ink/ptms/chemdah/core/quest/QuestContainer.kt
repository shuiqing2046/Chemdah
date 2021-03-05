package ink.ptms.chemdah.core.quest

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.api.event.QuestEvents
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.AgentType.Companion.toAgentType
import ink.ptms.chemdah.core.quest.option.Meta
import ink.ptms.chemdah.core.quest.option.MetaReset.Companion.reset
import ink.ptms.chemdah.core.quest.option.MetaType
import ink.ptms.chemdah.util.asList
import ink.ptms.chemdah.util.mirrorFuture
import ink.ptms.chemdah.util.namespaceQuest
import ink.ptms.chemdah.util.print
import io.izzel.taboolib.kotlin.kether.KetherShell
import io.izzel.taboolib.util.Coerce
import io.izzel.taboolib.util.Reflection
import org.bukkit.configuration.ConfigurationSection
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.MetaContainer
 *
 * @author sky
 * @since 2021/3/4 12:45 上午
 */
abstract class QuestContainer(val id: String, val config: ConfigurationSection) {

    protected val meta = HashMap<String, Meta<*>>()
    protected val agent: List<Agent>

    init {
        config.getConfigurationSection("meta")?.getKeys(false)?.forEach {
            val questMeta = ChemdahAPI.getQuestMeta(it)
            if (questMeta != null) {
                val metaType = if (questMeta.isAnnotationPresent(MetaType::class.java)) {
                    questMeta.getAnnotation(MetaType::class.java).type
                } else {
                    MetaType.Type.ANY
                }
                meta[it] = Reflection.instantiateObject(questMeta, metaType[config, "meta.$it"], this) as Meta<*>
            }
        }
        agent = config.getKeys(false)
            .filter { it.startsWith("agent(") && it.endsWith(")") }
            .map {
                val args = it.substring("agent(".length, it.length - 1).split("&").map { a -> a.trim() }
                val type = when (this) {
                    is Template -> "quest_${args[0]}"
                    is Task -> "task_${args[0]}"
                    else -> args[0]
                }
                Agent(
                    type.toAgentType(),
                    config.get(it)!!.asList(),
                    Coerce.toInteger(args.getOrNull(1))
                )
            }.sortedByDescending { it.priority }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Meta<*>> meta(metaId: String): T? {
        return meta[metaId] as? T?
    }

    /**
     * 获取正在进行中的所属任务
     */
    fun getQuest(profile: PlayerProfile): Quest? {
        return when (this) {
            is Template -> profile.quests.firstOrNull { it.id == id }
            is Task -> template.getQuest(profile)
            else -> null
        }
    }

    /**
     * 检查重置条件
     */
    fun checkReset(profile: PlayerProfile): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()
        mirrorFuture("QuestContainer:checkReset") {
            val reset = reset()
            if (reset.isEmpty()) {
                future.complete(false)
                finish()
            }
            KetherShell.eval(reset, namespace = namespaceQuest) {
                sender = profile.player
                rootFrame().variables().also { vars ->
                    vars.set("@Quest", getQuest(profile))
                    vars.set("@QuestContainer", this@QuestContainer)
                }
            }.thenApply {
                future.complete(Coerce.toBoolean(it))
                finish()
            }
        }
        return future
    }

    /**
     * 指定脚本代理
     * 当高优先级的脚本代理取消行为时后续脚本代理将不再运行
     *
     * @param profile 玩家数据
     * @param agentType 脚本代理类型
     */
    fun agent(profile: PlayerProfile, agentType: AgentType, quest: Quest? = null): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()
        mirrorFuture("QuestContainer:agent") {
            if (QuestEvents.Agent(this@QuestContainer, profile, agentType).call().isCancelled) {
                future.complete(false)
                finish()
            }
            val agents = agent.filter { it.type == agentType }
            fun process(cur: Int) {
                if (cur < agents.size) {
                    try {
                        KetherShell.eval(agents[cur].action, namespace = agentType.namespaceAll()) {
                            sender = profile.player
                            rootFrame().variables().also { vars ->
                                vars.set("@Quest", quest)
                                vars.set("@QuestContainer", this@QuestContainer)
                            }
                        }.thenApply {
                            if (it is Boolean && !it) {
                                future.complete(false)
                            } else {
                                process(cur + 1)
                            }
                        }
                    } catch (e: Throwable) {
                        e.print()
                    }
                } else {
                    future.complete(true)
                }
            }
            process(0)
            finish()
        }
        return future
    }
}