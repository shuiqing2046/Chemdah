package ink.ptms.chemdah.module.command

import com.google.common.collect.ImmutableList
import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.util.print
import io.izzel.taboolib.kotlin.kether.ScriptContext
import io.izzel.taboolib.kotlin.sendLocale
import io.izzel.taboolib.module.command.base.*
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

/**
 * @Author sky
 * @Since 2020-08-05 0:05
 */
@BaseCommand(name = "ChemdahScript", aliases = ["chs"], permission = "chemdah.command")
class CommandChemdahScript : BaseMainCommand() {

    val workspace = ChemdahAPI.workspace

    override fun onTabComplete(sender: CommandSender, command: String, argument: String): List<String>? {
        return when (argument) {
            "@command-argument-script" -> workspace.scripts.map { it.value.id }
            else -> null
        }
    }

    @SubCommand(description = "@command-script-run", arguments = ["@command-argument-script", "@command-argument-viewer?"], priority = 0.0)
    fun run(sender: CommandSender, args: Array<String>) {
        val script = workspace.scripts[args[0]]
        if (script != null) {
            val context = ScriptContext.create(script) {
                if (args.size > 1) {
                    this.sender = Bukkit.getPlayerExact(args[1])
                }
            }
            try {
                workspace.runScript(args[0], context)
            } catch (t: Throwable) {
                sender.sendLocale("command-script-error", t.localizedMessage)
                t.print()
            }
        } else {
            sender.sendLocale("command-script-not-found")
        }
    }

    @SubCommand(description = "@command-script-stop", arguments = ["@command-argument-script"], priority = 0.1)
    fun stop(sender: CommandSender, args: Array<String>) {
        val script = ImmutableList.copyOf(workspace.getRunningScript()).firstOrNull { it.quest.id == args[0] }
        if (script != null) {
            workspace.terminateScript(script)
        } else {
            sender.sendLocale("command-script-not-running")
        }
    }

    @SubCommand(description = "@command-script-stop-all", arguments = ["@command-argument-script?"], priority = 0.2)
    fun stopall(sender: CommandSender, args: Array<String>) {
        val script = ImmutableList.copyOf(workspace.getRunningScript()).filter { args.isEmpty() || it.quest.id == args[0] }
        if (script.isNotEmpty()) {
            script.forEach { workspace.terminateScript(it) }
        } else {
            sender.sendLocale("command-script-not-running")
        }
    }

    @SubCommand(description = "@command-script-list", priority = 0.3)
    fun list(sender: CommandSender, args: Array<String>) {
        sender.sendLocale("command-script-list-all",
            workspace.scripts.map { it.value.id }.joinToString(", "),
            workspace.getRunningScript().joinToString(", ") { it.id }
        )
    }

    @SubCommand(description = "@command-script-reload", priority = 0.4)
    fun reload(sender: CommandSender, args: Array<String>) {
        workspace.cancelAll()
        workspace.loadAll()
        sender.sendLocale("command-script-reload-all")
    }
}