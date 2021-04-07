package ink.ptms.chemdah.module.command

import ink.ptms.chemdah.api.ChemdahAPI
import io.izzel.taboolib.module.command.base.BaseCommand
import io.izzel.taboolib.module.command.base.BaseMainCommand
import io.izzel.taboolib.module.command.base.SubCommand
import io.izzel.taboolib.module.locale.TLocale
import org.bukkit.command.CommandSender

/**
 * Chemdah
 * ink.ptms.chemdah.command.CommandChemdahVariables
 *
 * @author sky
 * @since 2021/2/11 7:19 下午
 */
@BaseCommand(name = "chemdahvariables", aliases = ["chv"], permission = "chemdah.command")
class CommandChemdahVariables : BaseMainCommand() {

    override fun onTabComplete(sender: CommandSender, command: String, argument: String): List<String>? {
        return when (argument) {
            "@command-argument-key" -> ChemdahAPI.getVariables()
            else -> null
        }
    }

    @SubCommand(description = "@command-variables-get", arguments = ["@command-argument-key"], priority = 0.0)
    fun get(sender: CommandSender, args: Array<String>) {
        val time = System.currentTimeMillis()
        TLocale.sendTo(sender, "command-variables-change", "${args[0]} §8== §f${ChemdahAPI.getVariable(args[0])} §7(${System.currentTimeMillis() - time}ms)")
    }

    @SubCommand(description = "@command-variables-set", arguments = ["@command-argument-key", "@command-argument-value"], priority = 1.0)
    fun set(sender: CommandSender, args: Array<String>) {
        val time = System.currentTimeMillis()
        ChemdahAPI.setVariable(args[0], args[1])
        TLocale.sendTo(sender, "command-variables-change", "${args[0]} §8+= §f${args[1]} §7(${System.currentTimeMillis() - time}ms)")
    }

    @SubCommand(description = "@command-variables-add", arguments = ["@command-argument-key", "@command-argument-value"], priority = 1.1)
    fun add(sender: CommandSender, args: Array<String>) {
        val time = System.currentTimeMillis()
        ChemdahAPI.setVariable(args[0], args[1], true)
        TLocale.sendTo(sender, "command-variables-change", "${args[0]} §8= §f${args[1]} §7(${System.currentTimeMillis() - time}ms)")
    }

    @SubCommand(description = "@command-variables-remove", arguments = ["@command-argument-key"], priority = 1.2)
    fun remove(sender: CommandSender, args: Array<String>) {
        val time = System.currentTimeMillis()
        ChemdahAPI.setVariable(args[0], null)
        TLocale.sendTo(sender, "command-variables-change", "${args[0]} §8= §fnull §7(${System.currentTimeMillis() - time}ms)")
    }

    @SubCommand(description = "@command-variables-list", priority = 1.3)
    fun list(sender: CommandSender, args: Array<String>) {
        val time = System.currentTimeMillis()
        TLocale.sendTo(sender, "command-variables-list-header", ChemdahAPI.getVariables())
        TLocale.sendTo(sender, "command-variables-change", "${System.currentTimeMillis() - time}ms")
    }
}