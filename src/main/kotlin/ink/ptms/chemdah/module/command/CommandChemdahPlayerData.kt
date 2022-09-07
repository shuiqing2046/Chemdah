package ink.ptms.chemdah.module.command

import ink.ptms.chemdah.api.ChemdahAPI.chemdahProfile
import ink.ptms.chemdah.util.increaseAny
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import taboolib.common.platform.command.*
import taboolib.common.platform.function.onlinePlayers
import taboolib.expansion.createHelper
import taboolib.platform.util.sendLang

/**
 * Chemdah
 * ink.ptms.chemdah.command.CommandChemdahPlayerData
 *
 * @author sky
 * @since 2021/2/11 7:19 下午
 */
@CommandHeader(name = "ChemdahPlayerData", aliases = ["chpd"], permission = "chemdah.command")
object CommandChemdahPlayerData {

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

    @CommandBody
    val set = subCommand {
        dynamic(commit = "player") {
            suggestPlayers()
            dynamic(commit = "key") {
                dynamic(commit = "value") {
                    execute<CommandSender> { sender, context, argument ->
                        val playerExact = Bukkit.getPlayerExact(context.argument(-2))!!
                        playerExact.chemdahProfile.persistentDataContainer[context.argument(-1)] = argument
                        sender.sendLang("command-variables-change", "${context.argument(-1)} §8= §f${argument}")
                    }
                }
            }
        }
    }

    @CommandBody
    val add = subCommand {
        dynamic(commit = "player") {
            suggestPlayers()
            dynamic(commit = "key") {
                dynamic(commit = "value") {
                    execute<CommandSender> { sender, context, argument ->
                        val playerExact = Bukkit.getPlayerExact(context.argument(-2))!!
                        val key = context.argument(-1)
                        val persistentDataContainer = playerExact.chemdahProfile.persistentDataContainer
                        persistentDataContainer[key] = persistentDataContainer[key].increaseAny(argument)
                        sender.sendLang("command-variables-change", "$key §8+= §f${argument}")
                    }
                }
            }
        }
    }

    @CommandBody
    val remove = subCommand {
        dynamic(commit = "player") {
            suggestPlayers()
            dynamic(commit = "key") {
                execute<CommandSender> { sender, context, argument ->
                    val playerExact = Bukkit.getPlayerExact(context.argument(-1))!!
                    if (argument == "*") {
                        playerExact.chemdahProfile.persistentDataContainer.clear()
                        sender.sendLang("command-variables-change", "CLEAR")
                    } else {
                        playerExact.chemdahProfile.persistentDataContainer.remove(argument)
                        sender.sendLang("command-variables-change", "$argument §8= §fnull")
                    }
                }
            }
        }
    }

    @CommandBody
    val clear = subCommand {
        dynamic(commit = "player") {
            suggestPlayers()
            execute<CommandSender> { sender, _, argument ->
                val playerExact = Bukkit.getPlayerExact(argument)!!
                playerExact.chemdahProfile.persistentDataContainer.clear()
                sender.sendLang("command-variables-change", "* §8= §fnull")
            }
        }
    }
}