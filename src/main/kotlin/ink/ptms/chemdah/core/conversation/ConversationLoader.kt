package ink.ptms.chemdah.core.conversation

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.core.conversation.AgentType.Companion.toAgentType
import ink.ptms.chemdah.core.conversation.theme.ThemeChat
import ink.ptms.chemdah.core.conversation.theme.ThemeChest
import ink.ptms.chemdah.util.asMap
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.info
import taboolib.common.platform.function.releaseResourceFile
import taboolib.common.platform.function.warning
import taboolib.common.util.asList
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.SecuredFile
import java.io.File

/**
 * Chemdah
 * ink.ptms.chemdah.core.conversation.ConversationLoader
 *
 * @author sky
 * @since 2021/2/9 6:34 下午
 */
object ConversationLoader {

    init {
        ChemdahAPI.addConversationTheme("chat", ThemeChat)
        ChemdahAPI.addConversationTheme("chest", ThemeChest)
    }

    @Awake(LifeCycle.ENABLE)
    fun load() {
        val file = File(getDataFolder(), "core/conversation")
        if (!file.exists()) {
            releaseResourceFile("core/conversation/example.yml", true)
        }
        val conversations = load(file)
        ChemdahAPI.conversation.clear()
        ChemdahAPI.conversation.putAll(conversations.map { it.id to it })
        ChemdahAPI.conversationTheme.values.forEach { it.reloadConfig() }
        info("${ChemdahAPI.conversation.size} conversations loaded.")
        // 重复检查
        conversations.groupBy { it.id }.forEach { (id, c) ->
            if (c.size > 1) {
                warning("${c.size} conversations use duplicate id: $id")
            }
        }
    }

    fun load(file: Configuration): List<Conversation> {
        val option = if (file.isConfigurationSection("__option__")) {
            Option(file.getConfigurationSection("__option__")!!)
        } else {
            Option.default
        }
        return file.getKeys(false).filter { it != "__option__" && file.isConfigurationSection(it) }.map {
            load(null, option, file.getConfigurationSection(it)!!)
        }
    }

    fun load(file: File): List<Conversation> {
        return when {
            file.isDirectory -> {
                file.listFiles()?.flatMap { load(it) }?.toList() ?: emptyList()
            }
            file.name.endsWith(".yml") -> {
                SecuredFile.loadConfiguration(file).run {
                    val option = if (isConfigurationSection("__option__")) {
                        Option(getConfigurationSection("__option__")!!)
                    } else {
                        Option.default
                    }
                    getKeys(false).filter { it != "__option__" && isConfigurationSection(it) }.map {
                        load(file, option, getConfigurationSection(it)!!)
                    }
                }
            }
            else -> {
                emptyList()
            }
        }
    }

    private fun load(file: File?, option: Option, root: ConfigurationSection): Conversation {
        return Conversation(
            root.name,
            file,
            root,
            root["npc id"]?.run {
                Trigger(asList().map {
                    it.split(" ")
                }.filter {
                    it.size == 2
                }.map {
                    Trigger.Id(it[0], it[1])
                })
            } ?: Trigger(emptyList()),
            root.getStringList("npc").toMutableList(),
            root.getList("player")?.run {
                PlayerSide(mapNotNull { it.asMap() }.map {
                    PlayerReply(
                        it.toMutableMap(),
                        it["if"]?.toString(),
                        it["reply"].toString(),
                        it["then"]?.asList()?.toMutableList() ?: ArrayList()
                    )
                }.toMutableList())
            } ?: PlayerSide(ArrayList()),
            root.getString("condition"),
            root.getKeys(false)
                .filter { it.startsWith("agent:") }
                .map {
                    val args = it.substring("agent:".length).split("@").map { a -> a.trim() }
                    Agent(
                        args[0].toAgentType(),
                        root.get(it)!!.asList(),
                        args.getOrNull(1) ?: "self"
                    )
                }.toMutableList(),
            option
        )
    }
}