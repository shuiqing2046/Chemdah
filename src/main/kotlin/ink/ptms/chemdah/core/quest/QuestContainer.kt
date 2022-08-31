package ink.ptms.chemdah.core.quest

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.api.event.collect.QuestEvents
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.AgentType.Companion.toAgentType
import ink.ptms.chemdah.core.quest.addon.Addon
import ink.ptms.chemdah.core.quest.meta.Meta
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.platform.function.warning
import taboolib.common.util.asList
import taboolib.library.configuration.ConfigurationSection
import taboolib.library.reflex.Reflex.Companion.invokeConstructor
import taboolib.module.kether.KetherShell
import taboolib.module.kether.printKetherErrorMessage
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.MetaContainer
 *
 * @author sky
 * @since 2021/3/4 12:45 上午
 */
abstract class QuestContainer(val id: String, val config: ConfigurationSection) {

    /**
     * 元数据容器
     */
    val metaMap = HashMap<String, Meta<*>>()

    /**
     * 扩展列表
     */
    val addonMap = HashMap<String, Addon>()

    /**
     * 脚本代理列表
     */
    val agentList = ArrayList<Agent>()

    /**
     * 返回所有脚本代理类型
     */
    val agents: List<String>
        get() = agentList.map { "${it.type.name} @ ${it.restrict}" }

    /**
     * 当前所属任务节点
     */
    val node: String
        get() = when (this) {
            is Template -> id
            is Task -> template.id
            else -> "null"
        }

    /**
     * 任务路径
     * 作为持久化储存的唯一标识符
     */
    val path: String
        get() = when (this) {
            is Template -> id
            is Task -> "${template.id}.${id}"
            else -> "null"
        }

    init {
        // 简化写法 不再支持 eo-yaml 库
//        config.getKeys(false).filter { it.startsWith("agent:") }.forEach { loadAgent(it.substring("agent:".length), config.get(it)!!) }
//        config.getKeys(false).filter { it.startsWith("addon:") }.forEach { loadAddon(it.substring("addon:".length), it) }
//        config.getKeys(false).filter { it.startsWith("meta:") }.forEach { loadAddon(it.substring("meta:".length), it) }
        // 容错写法
        config.getConfigurationSection("agent")?.getKeys(false)?.forEach { node -> loadAgent(node, config.get("agent.$node")!!) }
        config.getConfigurationSection("addon")?.getKeys(false)?.forEach { node -> loadAddon(node) }
        config.getConfigurationSection("meta")?.getKeys(false)?.forEach { node -> loadMeta(node) }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Meta<*>> meta(metaId: String): T? {
        return metaMap[metaId] as? T?
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Addon> addon(addonId: String): T? {
        return addonMap[addonId] as? T?
    }

    /**
     * 获取正在进行中的所属任务
     */
    fun getQuest(profile: PlayerProfile, openAPI: Boolean = false): Quest? {
        return when (this) {
            is Template -> profile.getQuests(openAPI).firstOrNull { it.id == id }
            is Task -> template.getQuest(profile, openAPI)
            else -> null
        }
    }

    /**
     * 获取有效的脚本代理列表
     */
    fun getAgentList(agentType: AgentType, restrict: String = "self"): List<Agent> {
        return agentList.filter { it.type == agentType && (it.restrict == "*" || it.restrict == "all" || it.restrict == restrict) }
    }

    /**
     * 指定脚本代理
     * 当高优先级的脚本代理取消行为时后续脚本代理将不再运行
     */
    fun agent(profile: PlayerProfile, agentType: AgentType, restrict: String = "self", reason: String? = null): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()
        if (!QuestEvents.Agent(this@QuestContainer, profile, agentType, restrict).call()) {
            future.complete(false)
            return future
        }
        val agent = getAgentList(agentType, restrict)
        fun process(cur: Int) {
            if (cur < agent.size) {
                try {
                    KetherShell.eval(agent[cur].action, sender = adaptPlayer(profile.player), namespace = agentType.namespaceAll()) {
                        rootFrame().variables().also { vars ->
                            vars.set("reason", reason)
                            vars.set("@QuestSelected", node)
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
                    warning("path: $path, agentType: $agentType, source: ${agent[cur].action}")
                    e.printKetherErrorMessage()
                }
            } else {
                future.complete(true)
            }
        }
        process(0)
        return future
    }

    private fun loadAgent(source: String, value: Any) {
        val args = source.split("@").map { a -> a.trim() }
        val type = when (this) {
            is Template -> "quest_${args[0]}"
            is Task -> "task_${args[0]}"
            else -> args[0]
        }
        if (type.toAgentType() != AgentType.NONE) {
            agentList.add(Agent(type.toAgentType(), value.asList(), args.getOrElse(1) { "self" }))
        } else {
            warning("${args[0]} agent not supported.")
        }
    }

    private fun loadAddon(addonId: String, addonNode: String = "addon.$addonId") {
        val addon = ChemdahAPI.getQuestAddon(addonId)
        if (addon != null) {
            val option = if (addon.isAnnotationPresent(Option::class.java)) {
                addon.getAnnotation(Option::class.java).type
            } else {
                Option.Type.ANY
            }
            addonMap[addonId] = addon.invokeConstructor(option[config, addonNode], this)
        } else {
            warning("$addonId addon not supported.")
        }
    }

    private fun loadMeta(metaId: String, metaNode: String = "meta.$metaId") {
        val meta = ChemdahAPI.getQuestMeta(metaId)
        if (meta != null) {
            val option = if (meta.isAnnotationPresent(Option::class.java)) {
                meta.getAnnotation(Option::class.java).type
            } else {
                Option.Type.ANY
            }
            metaMap[metaId] = meta.invokeConstructor(option[config, metaNode], this) as Meta<*>
        } else {
            warning("$metaId meta not supported.")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is QuestContainer) return false
        if (path != other.path) return false
        return true
    }

    override fun hashCode(): Int {
        return path.hashCode()
    }
}