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

    @SubCommand(description = "@command-script-run")
    var run: BaseSubCommand = object : BaseSubCommand() {

        override fun getArguments(): Array<Argument> {
            return of(
                Argument("@command-argument-script") { workspace.scripts.map { it.value.id } },
                Argument("@command-argument-viewer", false)
            )
        }

        override fun onCommand(sender: CommandSender, command: org.bukkit.command.Command, s: String, args: Array<String>) {
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
    }

    @SubCommand(description = "@command-script-stop")
    var stop: BaseSubCommand = object : BaseSubCommand() {

        override fun getArguments(): Array<Argument> {
            return of(
                Argument("@command-argument-script") { workspace.scripts.map { it.value.id } },
                Argument("@command-argument-viewer", false)
            )
        }

        override fun onCommand(sender: CommandSender, command: org.bukkit.command.Command, s: String, args: Array<String>) {
            val script = ImmutableList.copyOf(workspace.getRunningScript()).firstOrNull { it.quest.id == args[0] }
            if (script != null) {
                workspace.terminateScript(script)
            } else {
                sender.sendLocale("command-script-not-running")
            }
        }
    }

    @SubCommand(description = "@command-script-stop-all")
    var stopall: BaseSubCommand = object : BaseSubCommand() {

        override fun getArguments(): Array<Argument> {
            return of(
                Argument("@command-argument-script", false) { workspace.scripts.map { it.value.id } },
                Argument("@command-argument-viewer", false)
            )
        }

        override fun onCommand(sender: CommandSender, command: org.bukkit.command.Command, s: String, args: Array<String>) {
            val script = ImmutableList.copyOf(workspace.getRunningScript()).filter { args.isEmpty() || it.quest.id == args[0] }
            if (script.isNotEmpty()) {
                script.forEach { workspace.terminateScript(it) }
            } else {
                sender.sendLocale("command-script-not-running")
            }
        }
    }

    @SubCommand(description = "@command-script-list")
    var list: BaseSubCommand = object : BaseSubCommand() {

        override fun onCommand(sender: CommandSender, command: org.bukkit.command.Command, s: String, args: Array<String>) {
            sender.sendLocale("command-script-list-all",
                workspace.scripts.map { it.value.id }.joinToString(", "),
                workspace.getRunningScript().joinToString(", ") { it.id }
            )
        }
    }

    @SubCommand(description = "@command-script-reload")
    var reload: BaseSubCommand = object : BaseSubCommand() {

        override fun onCommand(sender: CommandSender, command: org.bukkit.command.Command, s: String, args: Array<String>) {
            workspace.cancelAll()
            workspace.loadAll()
            sender.sendLocale("command-script-reload-all")
        }
    }
}