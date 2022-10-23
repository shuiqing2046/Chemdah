package ink.ptms.chemdah.module.command

import ink.ptms.adyeshach.api.AdyeshachAPI
import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.core.conversation.Source
import ink.ptms.chemdah.core.conversation.trigger.TriggerAdyeshach.openConversation
import ink.ptms.chemdah.module.scenes.ScenesSystem
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.*
import taboolib.common5.Coerce
import taboolib.expansion.createHelper
import taboolib.module.chat.colored
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
        dynamic(comment = "player") {
            suggestPlayers()
            dynamic(comment = "scenes") {
                suggestion<CommandSender> { _, _ -> ScenesSystem.scenesMap.keys.toMutableList() }
                dynamic(comment = "index") {
                    execute<CommandSender> { sender, ctx, argument ->
                        val scenesFile = ScenesSystem.scenesMap[ctx.argument(-1)]
                        if (scenesFile == null) {
                            sender.sendLang("command-scenes-file-not-found")
                            return@execute
                        }
                        scenesFile.state.firstOrNull { it.index == Coerce.toInteger(argument) }?.send(ctx.player(-2).cast())
                    }
                }
            }
        }
    }

    @CommandBody
    val cancelscenes = subCommand {
        dynamic(comment = "player") {
            suggestPlayers()
            dynamic(comment = "scenes") {
                suggestion<CommandSender> { _, _ -> ScenesSystem.scenesMap.keys.toMutableList() }
                dynamic(comment = "index") {
                    execute<CommandSender> { sender, ctx, argument ->
                        val scenesFile = ScenesSystem.scenesMap[ctx.argument(-1)]
                        if (scenesFile == null) {
                            sender.sendLang("command-scenes-file-not-found")
                            return@execute
                        }
                        scenesFile.state.firstOrNull { it.index == Coerce.toInteger(argument) }?.cancel(ctx.player(-2).cast())
                    }
                }
            }
        }
    }

    @CommandBody
    val conversation = subCommand {
        literal("npc") {
            dynamic(comment = "player") {
                suggestPlayers()
                dynamic(comment = "id") {
                    suggestion<CommandSender> { _, ctx ->
                        AdyeshachAPI.getVisibleEntities(ctx.player(-1).cast()).map { it.id }
                    }
                    execute<CommandSender> { sender, ctx, argument ->
                        val player = ctx.player(-1).cast<Player>()
                        val npc = AdyeshachAPI.getVisibleEntities(player).firstOrNull { it.id == argument }
                        if (npc == null) {
                            sender.sendLang("command-adyeshach-not-found")
                            return@execute
                        }
                        npc.openConversation(player)
                    }
                }
            }
        }
        literal("self") {
            dynamic(comment = "player") {
                suggestPlayers()
                dynamic(comment = "id") {
                    suggestion<CommandSender> { _, _ -> ChemdahAPI.conversation.keys.toList() }
                    dynamic(comment = "name") {
                        execute<CommandSender> { sender, ctx, argument ->
                            val conversation = ChemdahAPI.conversation[ctx.argument(-1)]
                            if (conversation == null) {
                                sender.sendLang("command-conversation-not-found")
                                return@execute
                            }
                            conversation.openSelf(ctx.player(-2).cast(), argument.colored())
                        }
                    }
                }
            }
        }
    }

//    @CommandBody
//    val generate = subCommand {
//        execute<CommandSender> { sender, _, _ ->
//            val json = Configuration.empty(Type.JSON)
//            ChemdahAPI.questObjective.values.sortedBy { it.name }.forEach {
//                val plugin = if (it.javaClass.isAnnotationPresent(Dependency::class.java)) {
//                    val plugin = it.javaClass.getAnnotation(Dependency::class.java).plugin
//                    if (plugin == "minecraft") "Minecraft" else plugin
//                } else {
//                    "Minecraft"
//                }
//                json["objective.$plugin.${it.name}.condition"] = it.conditions.keys
//                json["objective.$plugin.${it.name}.goal"] = it.goals.keys.flatMap { k -> k.split(",") }.filter { k -> k != "null" }
//            }
//            val file = newFile(getDataFolder(), "api.json")
//            json.saveToFile(file)
//            sender.sendMessage("Generated api file: ${file.path}")
//        }
//    }
}