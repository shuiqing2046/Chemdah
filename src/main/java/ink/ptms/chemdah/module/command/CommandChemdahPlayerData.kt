package ink.ptms.chemdah.module.command

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.api.ChemdahAPI.chemdahProfile
import ink.ptms.chemdah.util.getProfile
import ink.ptms.chemdah.util.increaseAny
import io.izzel.taboolib.kotlin.kether.action.bukkit.Symbol
import io.izzel.taboolib.module.command.base.BaseCommand
import io.izzel.taboolib.module.command.base.BaseMainCommand
import io.izzel.taboolib.module.command.base.SubCommand
import io.izzel.taboolib.module.locale.TLocale
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

/**
 * Chemdah
 * ink.ptms.chemdah.command.CommandChemdahPlayerData
 *
 * @author sky
 * @since 2021/2/11 7:19 下午
 */
@BaseCommand(name = "ChemdahPlayerData", aliases = ["chpd"], permission = "chemdah.command")
class CommandChemdahPlayerData : BaseMainCommand() {

    @SubCommand(
        description = "@command-variables-set",
        arguments = ["@command-argument-player", "@command-argument-key", "@command-argument-value"],
        priority = 1.0
    )
    fun set(sender: CommandSender, args: Array<String>) {
        val playerExact = Bukkit.getPlayerExact(args[0])
        if (playerExact == null) {
            TLocale.sendTo(sender, "command-player-not-found")
            return
        }
        playerExact.chemdahProfile.persistentDataContainer[args[1]] = args[2]
        TLocale.sendTo(sender, "command-variables-change", "${args[1]} §8= §f${args[2]}")
    }

    @SubCommand(
        description = "@command-variables-add",
        arguments = ["@command-argument-player", "@command-argument-key", "@command-argument-value"],
        priority = 1.1
    )
    fun add(sender: CommandSender, args: Array<String>) {
        val playerExact = Bukkit.getPlayerExact(args[0])
        if (playerExact == null) {
            TLocale.sendTo(sender, "command-player-not-found")
            return
        }
        val persistentDataContainer = playerExact.chemdahProfile.persistentDataContainer
        persistentDataContainer[args[1]] = persistentDataContainer[args[1]].increaseAny(args[2])
        TLocale.sendTo(sender, "command-variables-change", "${args[1]} §8+= §f${args[2]}")
    }

    @SubCommand(description = "@command-variables-remove", arguments = ["@command-argument-player", "@command-argument-key"], priority = 1.2)
    fun remove(sender: CommandSender, args: Array<String>) {
        val playerExact = Bukkit.getPlayerExact(args[0])
        if (playerExact == null) {
            TLocale.sendTo(sender, "command-player-not-found")
            return
        }
        playerExact.chemdahProfile.persistentDataContainer.remove(args[1])
        TLocale.sendTo(sender, "command-variables-change", "${args[1]} §8= §fnull")
    }

    @SubCommand(description = "@command-variables-clear", arguments = ["@command-argument-player"], priority = 1.3)
    fun clear(sender: CommandSender, args: Array<String>) {
        val playerExact = Bukkit.getPlayerExact(args[0])
        if (playerExact == null) {
            TLocale.sendTo(sender, "command-player-not-found")
            return
        }
        playerExact.chemdahProfile.persistentDataContainer.clear();
        TLocale.sendTo(sender, "command-variables-change", "* §8= §fnull")
    }
}