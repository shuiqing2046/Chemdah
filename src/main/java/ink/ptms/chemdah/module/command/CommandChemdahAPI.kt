package ink.ptms.chemdah.module.command

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.api.ChemdahAPI.chemdahProfile
import ink.ptms.chemdah.api.ChemdahAPI.mirror
import io.izzel.taboolib.kotlin.Tasks
import io.izzel.taboolib.kotlin.sendLocale
import io.izzel.taboolib.module.command.base.BaseCommand
import io.izzel.taboolib.module.command.base.BaseMainCommand
import io.izzel.taboolib.module.command.base.SubCommand
import io.izzel.taboolib.module.locale.TLocale
import io.izzel.taboolib.util.Coerce
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

/**
 * Chemdah
 * ink.ptms.chemdah.command.ChemdahCommand
 *
 * @author sky
 * @since 2021/2/11 7:19 下午
 */
@BaseCommand(name = "ChemdahAPI", aliases = ["chapi"], permission = "chemdah.command")
class CommandChemdahAPI : BaseMainCommand() {

    @SubCommand(description = "@command-api-scenes", arguments = ["@command-argument-player", "@command-argument-name", "@command-argument-state"], priority = 1.0)
    fun createscenes(sender: CommandSender, args: Array<String>) {
    }

    @SubCommand(description = "@command-api-scenes", arguments = ["@command-argument-player", "@command-argument-name"], priority = 1.1)
    fun cancelscenes(sender: CommandSender, args: Array<String>) {
    }
}