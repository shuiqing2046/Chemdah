package ink.ptms.chemdah.module.command

import ink.ptms.chemdah.module.scenes.ScenesSystem
import taboolib.common5.Coerce
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.onlinePlayers
import taboolib.platform.util.sendLang

/**
 * Chemdah
 * ink.ptms.chemdah.command.ChemdahCommand
 *
 * @author sky
 * @since 2021/2/11 7:19 下午
 */
@CommandHeader(name = "ChemdahAPI", aliases = ["chapi"], permission = "chemdah.command")
object CommandChemdahAPI {

    @CommandBody
    val createscenes = subCommand {
        dynamic {
            suggestion<CommandSender> { _, _ -> onlinePlayers().map { it.name } }
            dynamic {
                suggestion<CommandSender> { _, _ -> ScenesSystem.scenesMap.keys.toMutableList() }
                dynamic {
                    execute<CommandSender> { sender, context, argument ->
                        val playerExact = Bukkit.getPlayerExact(context.argument(-2)!!)!!
                        val scenesFile = ScenesSystem.scenesMap[context.argument(-1)!!]
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
        dynamic {
            suggestion<CommandSender> { _, _ -> onlinePlayers().map { it.name } }
            dynamic {
                suggestion<CommandSender> { _, _ -> ScenesSystem.scenesMap.keys.toMutableList() }
                dynamic {
                    execute<CommandSender> { sender, context, argument ->
                        val playerExact = Bukkit.getPlayerExact(context.argument(-2)!!)!!
                        val scenesFile = ScenesSystem.scenesMap[context.argument(-1)!!]
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
}