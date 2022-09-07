package ink.ptms.chemdah.module.command

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.module.scenes.ScenesSystem
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import taboolib.common.io.newFile
import taboolib.common.platform.command.*
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.onlinePlayers
import taboolib.common5.Coerce
import taboolib.expansion.createHelper
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import taboolib.platform.util.sendLang

/**
 * Chemdah
 * ink.ptms.chemdah.command.ChemdahCommand
 *
 * @author sky
 * @since 2021/2/11 7:19 下午
 */
@Suppress("SpellCheckingInspection")
@CommandHeader(name = "ChemdahAPI", aliases = ["chapi"], permission = "chemdah.command")
object CommandChemdahAPI {

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

    @CommandBody
    val createscenes = subCommand {
        dynamic(commit = "player") {
            suggestPlayers()
            dynamic(commit = "scenes") {
                suggestion<CommandSender> { _, _ -> ScenesSystem.scenesMap.keys.toMutableList() }
                dynamic(commit = "index") {
                    execute<CommandSender> { sender, context, argument ->
                        val playerExact = Bukkit.getPlayerExact(context.argument(-2))!!
                        val scenesFile = ScenesSystem.scenesMap[context.argument(-1)]
                        if (scenesFile == null) {
                            sender.sendLang("command-scenes-file-not-found")
                            return@execute
                        }
                        scenesFile.state.firstOrNull { it.index == Coerce.toInteger(argument) }?.send(playerExact)
                    }
                }
            }
        }
    }

    @CommandBody
    val cancelscenes = subCommand {
        dynamic(commit = "player") {
            suggestPlayers()
            dynamic(commit = "scenes") {
                suggestion<CommandSender> { _, _ -> ScenesSystem.scenesMap.keys.toMutableList() }
                dynamic(commit = "index") {
                    execute<CommandSender> { sender, context, argument ->
                        val playerExact = Bukkit.getPlayerExact(context.argument(-2))!!
                        val scenesFile = ScenesSystem.scenesMap[context.argument(-1)]
                        if (scenesFile == null) {
                            sender.sendLang("command-scenes-file-not-found")
                            return@execute
                        }
                        scenesFile.state.firstOrNull { it.index == Coerce.toInteger(argument) }?.cancel(playerExact)
                    }
                }
            }
        }
    }

    @CommandBody
    val generate = subCommand {
        execute<CommandSender> { sender, _, _ ->
            val json = Configuration.empty(Type.JSON)
            ChemdahAPI.questObjective.values.sortedBy { it.name }.forEach {
                val plugin = if (it.javaClass.isAnnotationPresent(Dependency::class.java)) {
                    val plugin = it.javaClass.getAnnotation(Dependency::class.java).plugin
                    if (plugin == "minecraft") "Minecraft" else plugin
                } else {
                    "Minecraft"
                }
                json["objective.$plugin.${it.name}.condition"] = it.conditions.keys
                json["objective.$plugin.${it.name}.goal"] = it.goals.keys.flatMap { k -> k.split(",") }.filter { k -> k != "null" }
            }
            val file = newFile(getDataFolder(), "api.json")
            json.saveToFile(file)
            sender.sendMessage("Generated api file: ${file.path}")
        }
    }
}