package ink.ptms.chemdah.module.command

import ink.ptms.chemdah.api.ChemdahAPI
import org.bukkit.command.CommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.subCommand
import taboolib.platform.util.sendLang

/**
 * Chemdah
 * ink.ptms.chemdah.command.CommandChemdahVariables
 *
 * @author sky
 * @since 2021/2/11 7:19 下午
 */
@CommandHeader(name = "ChemdahVariables", aliases = ["chv"], permission = "chemdah.command")
object CommandChemdahVariables {

    @CommandBody
    val get = subCommand {
        dynamic(commit = "key") {
            suggestion<CommandSender> { _, _ -> ChemdahAPI.getVariables() }
            execute<CommandSender> { sender, _, argument ->
                val time = System.currentTimeMillis()
                sender.sendLang("command-variables-change", "$argument §8== §f${ChemdahAPI.getVariable(argument)} §7(${System.currentTimeMillis() - time}ms)")
            }
        }
    }

    @CommandBody
    val set = subCommand {
        dynamic(commit = "key") {
            suggestion<CommandSender>(uncheck = true) { _, _ -> ChemdahAPI.getVariables() }
            dynamic(commit = "value") {
                execute<CommandSender> { sender, context, argument ->
                    val time = System.currentTimeMillis()
                    ChemdahAPI.setVariable(context.argument(-1)!!, argument)
                    sender.sendLang("command-variables-change", "${context.argument(-1)!!} §8= §f$argument §7(${System.currentTimeMillis() - time}ms)")
                }
            }
        }
    }

    @CommandBody
    val add = subCommand {
        dynamic(commit = "key") {
            suggestion<CommandSender>(uncheck = true) { _, _ -> ChemdahAPI.getVariables() }
            dynamic(commit = "value") {
                execute<CommandSender> { sender, context, argument ->
                    val time = System.currentTimeMillis()
                    ChemdahAPI.setVariable(context.argument(-1)!!, argument, true)
                    sender.sendLang("command-variables-change", "${context.argument(-1)!!} §8+= §f$argument §7(${System.currentTimeMillis() - time}ms)")
                }
            }
        }
    }

    @CommandBody
    val remove = subCommand {
        dynamic(commit = "key") {
            suggestion<CommandSender> { _, _ -> ChemdahAPI.getVariables() }
            execute<CommandSender> { sender, _, argument ->
                val time = System.currentTimeMillis()
                ChemdahAPI.setVariable(argument, null)
                sender.sendLang("command-variables-change", "$argument §8= §fnull §7(${System.currentTimeMillis() - time}ms)")
            }
        }
    }

    @CommandBody
    val list = subCommand {
        execute<CommandSender> { sender, _, _ ->
            val time = System.currentTimeMillis()
            sender.sendLang("command-variables-list-header", ChemdahAPI.getVariables())
            sender.sendLang("command-variables-change", "${System.currentTimeMillis() - time}ms")
        }
    }
}