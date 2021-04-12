package ink.ptms.chemdah.core.conversation

import ink.ptms.chemdah.Chemdah
import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.core.conversation.AgentType.Companion.toAgentType
import ink.ptms.chemdah.util.asInt
import ink.ptms.chemdah.util.asList
import ink.ptms.chemdah.util.asMap
import ink.ptms.chemdah.util.colored
import io.izzel.taboolib.module.db.local.SecuredFile
import io.izzel.taboolib.module.inject.TFunction
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.FileConfiguration
import java.io.File

/**
 * Chemdah
 * ink.ptms.chemdah.core.conversation.ConversationLoader
 *
 * @author sky
 * @since 2021/2/9 6:34 下午
 */
object ConversationLoader {

    @TFunction.Init
    fun load() {
        val file = File(Chemdah.plugin.dataFolder, "core/conversation")
        if (!file.exists()) {
            Chemdah.plugin.saveResource("core/conversation/example.yml", true)
        }
        ChemdahAPI.conversation.clear()
        ChemdahAPI.conversation.putAll(load(file).map { it.id to it })
        ChemdahAPI.conversationTheme.values.forEach { it.reloadConfig() }
        println("[Chemdah] ${ChemdahAPI.conversation.size} conversation loaded.")
    }

    fun load(file: FileConfiguration): List<Conversation> {
        val option = if (file.isConfigurationSection("__option__")) {
            Option(file.getConfigurationSection("__option__")!!)
        } else {
            Option.default
        }
        return file.getKeys(false)
            .filter { it != "__option__" && file.isConfigurationSection(it) }
            .map {
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
            root.get("npc id")?.run {
                Trigger(asList().map {
                    it.split(" ")
                }.filter {
                    it.size == 2
                }.map {
                    Trigger.Id(it[0], it[1])
                })
            } ?: Trigger(emptyList()),
            root.getStringList("npc"),
            root.getList("player")?.run {
                PlayerSide(mapNotNull { it.asMap() }.map {
                    PlayerReply(
                        it,
                        it["if"]?.toString(),
                        it["reply"].toString().colored(),
                        it["then"]?.asList() ?: emptyList()
                    )
                })
            } ?: PlayerSide(emptyList()),
            root.getString("condition"),
            root.getKeys(false)
                .filter { it.startsWith("agent:") }
                .map {
                    val args = it.substring("agent:".length).split("&").map { a -> a.trim() }
                    Agent(
                        args[0].toAgentType(),
                        root.get(it)!!.asList(),
                        args.getOrNull(1).asInt()
                    )
                }.sortedByDescending { it.priority },
            option
        )
    }
}