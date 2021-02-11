package ink.ptms.chemdah.core.conversation

import ink.ptms.chemdah.Chemdah
import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.core.conversation.AgentType.Companion.toAgentType
import io.izzel.taboolib.module.db.local.SecuredFile
import io.izzel.taboolib.module.inject.TFunction
import io.izzel.taboolib.util.Coerce
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
        val file = File(Chemdah.plugin.dataFolder, "conversation")
        if (!file.exists()) {
            Chemdah.plugin.saveResource("conversation/example.yml", true)
        }
        ChemdahAPI.conversation.clear()
        ChemdahAPI.conversation.putAll(load(file).map { it.id to it })
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
        return if (file.isDirectory) {
            file.listFiles()?.flatMap { load(it) }?.toList() ?: emptyList()
        } else {
            SecuredFile.loadConfiguration(file).run {
                val option = if (isConfigurationSection("__option__")) {
                    Option(getConfigurationSection("__option__")!!)
                } else {
                    Option.default
                }
                getKeys(false)
                    .filter { it != "__option__" && isConfigurationSection(it) }
                    .map {
                        load(file, option, getConfigurationSection(it)!!)
                    }
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
                PlayerSide(mapNotNull {
                    when (it) {
                        is Map<*, *> -> it.map { (k, v) -> k.toString() to v }.toMap()
                        is ConfigurationSection -> it.getValues(false)
                        else -> null
                    }
                }.map {
                    PlayerReply(
                        it,
                        it["if"]?.toString(),
                        it["reply"].toString(),
                        it["then"]?.asList() ?: emptyList()
                    )
                })
            } ?: PlayerSide(emptyList()),
            root.getString("condition"),
            root.getKeys(false)
                .filter { it.startsWith("\$on") }
                .map {
                    val args = it.split(" ")
                    Agent(
                        args.getOrNull(1).toString().toAgentType(),
                        root.get(it)!!.asList(),
                        Coerce.toInteger(args.getOrNull(2))
                    )
                }.sortedByDescending { it.priority },
            option
        )
    }

    private fun Any.asList(): List<String> {
        return if (this !is List<*>) {
            listOf(toString())
        } else {
            map { it.toString() }
        }
    }
}