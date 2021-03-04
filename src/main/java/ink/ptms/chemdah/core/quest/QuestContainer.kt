package ink.ptms.chemdah.core.quest

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.api.event.QuestEvents
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.AgentType.Companion.toAgentType
import ink.ptms.chemdah.core.quest.meta.Meta
import ink.ptms.chemdah.core.quest.meta.MetaType
import ink.ptms.chemdah.core.script.print
import ink.ptms.chemdah.util.asList
import ink.ptms.chemdah.util.mirrorDefine
import ink.ptms.chemdah.util.mirrorFinish
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
abstract class QuestContainer(val config: ConfigurationSection) {

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
                Agent(
                    args[0].toAgentType(),
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
     * 指定脚本代理
     *
     * @param profile 玩家数据
     * @param agentType 脚本代理类型
     */
    fun agent(profile: PlayerProfile, agentType: AgentType, quest: Quest? = null): CompletableFuture<Boolean> {
        mirrorDefine("QuestContainer:agent")
        val future = CompletableFuture<Boolean>()
        if (QuestEvents.Agent(this, profile, agentType).call().isCancelled) {
            future.complete(false)
            mirrorFinish("QuestContainer:agent")
            return future
        }
        var successful = true
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
                            successful = false
                        }
                        process(cur + 1)
                    }
                } catch (e: Throwable) {
                    e.print()
                }
            } else {
                future.complete(successful)
            }
        }
        process(0)
        mirrorFinish("QuestContainer:agent")
        return future
    }
}