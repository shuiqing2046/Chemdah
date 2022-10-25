package ink.ptms.chemdah.module.command

import ink.ptms.chemdah.api.ChemdahAPI
import org.bukkit.command.CommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.expansion.createHelper
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
    val main = mainCommand {
        createHelper()
    }

    @CommandBody
    val get = subCommand {
        dynamic(comment ="key") {
            suggestion<CommandSender> { _, _ -> ChemdahAPI.getVariables() }
            execute<CommandSender> { sender, _, argument ->
                val time = System.currentTimeMillis()
                sender.sendLang("command-variables-change", "$argument §8== §f${ChemdahAPI.getVariable(argument)} §7(${System.currentTimeMillis() - time}ms)")
            }
        }
    }

    @CommandBody
    val set = subCommand {
        dynamic(comment ="key") {
            suggestion<CommandSender>(uncheck = true) { _, _ -> ChemdahAPI.getVariables() }
            dynamic(comment ="value") {
                execute<CommandSender> { sender, ctx, argument ->
                    val time = System.currentTimeMillis()
                    ChemdahAPI.setVariable(ctx.argument(-1), argument)
                    sender.sendLang("command-variables-change", "${ctx.argument(-1)} §8= §f$argument §7(${System.currentTimeMillis() - time}ms)")
                }
            }
        }
    }

    @CommandBody
    val add = subCommand {
        dynamic(comment ="key") {
            suggestion<CommandSender>(uncheck = true) { _, _ -> ChemdahAPI.getVariables() }
            dynamic(comment ="value") {
                execute<CommandSender> { sender, ctx, argument ->
                    val time = System.currentTimeMillis()
                    ChemdahAPI.setVariable(ctx.argument(-1), argument, true)
                    sender.sendLang("command-variables-change", "${ctx.argument(-1)} §8+= §f$argument §7(${System.currentTimeMillis() - time}ms)")
                }
            }
        }
    }

    @CommandBody
    val remove = subCommand {
        dynamic(comment ="key") {
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