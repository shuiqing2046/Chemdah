package ink.ptms.chemdah.module.command

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.api.ChemdahAPI.chemdahProfile
import ink.ptms.chemdah.api.ChemdahAPI.mirror
import ink.ptms.chemdah.module.scenes.ScenesSystem
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

    override fun onTabComplete(sender: CommandSender, command: String, argument: String): MutableList<String>? {
        return when (argument) {
            "@command-argument-name" -> ScenesSystem.scenesMap.keys.toMutableList()
            else -> null
        }
    }

    @SubCommand(
        description = "@command-api-scenes",
        arguments = ["@command-argument-player", "@command-argument-name", "@command-argument-state"],
        priority = 1.0
    )
    fun createscenes(sender: CommandSender, args: Array<String>) {
        val playerExact = Bukkit.getPlayerExact(args[0])
        if (playerExact == null) {
            TLocale.sendTo(sender, "command-player-not-found")
            return
        }
        val scenesFile = ScenesSystem.scenesMap[args[1]]
        if (scenesFile == null) {
            TLocale.sendTo(sender, "command-scenes-file-not-found")
            return
        }
        scenesFile.state.firstOrNull { it.index == Coerce.toInteger(args[2]) }?.send(playerExact)
    }

    @SubCommand(
        description = "@command-api-scenes",
        arguments = ["@command-argument-player", "@command-argument-name"],
        priority = 1.1
    )
    fun cancelscenes(sender: CommandSender, args: Array<String>) {
        val playerExact = Bukkit.getPlayerExact(args[0])
        if (playerExact == null) {
            TLocale.sendTo(sender, "command-player-not-found")
            return
        }
        val scenesFile = ScenesSystem.scenesMap[args[1]]
        if (scenesFile == null) {
            TLocale.sendTo(sender, "command-scenes-file-not-found")
            return
        }
        scenesFile.state.firstOrNull { it.index == Coerce.toInteger(args[2]) }?.cancel(playerExact)
    }
}