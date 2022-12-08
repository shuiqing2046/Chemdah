package ink.ptms.chemdah.core.conversation

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.api.event.collect.ConversationEvents
import ink.ptms.chemdah.core.conversation.AgentType.Companion.toAgent
import ink.ptms.chemdah.core.conversation.theme.ThemeChat
import ink.ptms.chemdah.core.conversation.theme.ThemeChest
import ink.ptms.chemdah.util.*
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.info
import taboolib.common.platform.function.releaseResourceFile
import taboolib.common.platform.function.warning
import taboolib.common.util.asList
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.Configuration
import java.io.File

/**
 * Chemdah
 * ink.ptms.chemdah.core.conversation.ConversationLoader
 *
 * @author sky
 * @since 2021/2/9 6:34 下午
 */
object ConversationLoader {

    var optionKey = "__option__"

    init {
        ChemdahAPI.addConversationTheme("chat", ThemeChat)
        ChemdahAPI.addConversationTheme("chest", ThemeChest)
    }

    @Awake(LifeCycle.ACTIVE)
    fun loadAll() {
        val file = File(getDataFolder(), "core/conversation")
        // 释放默认配置
        if (!file.exists()) {
            releaseResourceFile("core/conversation/example.yml", true)
        }
        // 加载对话文件
        val conversations = load(file)
        ChemdahAPI.conversation.clear()
        ChemdahAPI.conversation.putAll(conversations.map { it.id to it })
        ChemdahAPI.conversationTheme.values.forEach { it.reloadConfig() }
        info("${ChemdahAPI.conversation.size} conversations loaded.")
        // 检查重复对话序号
        conversations.groupBy { it.id }.forEach { (id, c) ->
            if (c.size > 1) {
                warning("${c.size} conversations use duplicate id: $id")
            }
        }
    }

    /**
     * 从文件中加载对话
     *
     * @param file 文件
     * @return [List<Conversation>]
     */
    fun load(file: File): List<Conversation> {
        return when {
            file.isDirectory -> file.listFiles()?.flatMap { load(it) }?.toList() ?: emptyList()
            file.extension == "yml" || file.extension == "json" -> load(Configuration.loadFromFile(file), file)
            else -> emptyList()
        }
    }

    /**
     * 从配置中加载对话
     *
     * @param conf 配置对象
     * @param file 配置文件
     * @return [List<Conversation>]
     */
    fun load(conf: Configuration, file: File?): List<Conversation> {
        val option = if (conf.isConfigurationSection(optionKey)) {
            Option(conf.getConfigurationSection(optionKey)!!)
        } else {
            Option.default
        }
        return conf.getKeys(false).filter { it != optionKey && conf.isConfigurationSection(it) }.mapNotNull {
            load(file, option, conf.getConfigurationSection(it)!!)
        }
    }

    private fun load(file: File?, option: Option, root: ConfigurationSection): Conversation? {
        if (ConversationEvents.Load(file, option, root).call()) {
            // 获取对话触发器
            val trigger = if (root["npc id"] != null) {
                Trigger(root.list("npc id").map { it.split(" ") }.filter { it.size == 2 }.map { Trigger.Id(it[0], it[1]) })
            } else {
                Trigger()
            }
            // 获取 NPC 发言内容
            val npcSide = root.list("npc").flatLines().toMutableList() // 兼容 Chemdah Lab
            // 获取 NPC 发言内容格式化
            val format = root.getString("format", "type")
            // 获取玩家回复内容
            val playerSide = root.mapListAs("player") { PlayerReply(it.toMutableMap()) }.to { PlayerSide(it) }
            // 获取代理
            val agents = root.sectionAs("agent", { it.split("@").trim() }) { k, v -> Agent(k[0].toAgent(), v.asList(), k.getOrElse(1) { "self" }) }
            // 创建对话
            return Conversation(root.name, file, root, trigger, npcSide, format, playerSide, root.getString("condition"), agents, option)
        }
        return null
    }
}