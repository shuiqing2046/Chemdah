package ink.ptms.chemdah.module.command

import ink.ptms.chemdah.api.ChemdahAPI.chemdahProfile
import ink.ptms.chemdah.module.level.LevelSystem
import ink.ptms.chemdah.module.level.LevelSystem.getLevel
import ink.ptms.chemdah.module.level.LevelSystem.setLevel
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
 * ink.ptms.chemdah.command.CommandChemdahPlayerLevel
 *
 * @author sky
 * @since 2021/2/11 7:19 下午
 */
@BaseCommand(name = "ChemdahPlayerLevel", aliases = ["chpl"], permission = "chemdah.command")
class CommandChemdahPlayerLevel : BaseMainCommand() {

    override fun onTabComplete(sender: CommandSender, command: String, argument: String): List<String>? {
        return when (argument) {
            "@command-argument-level" -> LevelSystem.level.keys.toList()
            else -> null
        }
    }

    @SubCommand(
        description = "@command-level-add-level",
        arguments = ["@command-argument-player", "@command-argument-level", "@command-argument-value"],
        priority = 1.0
    )
    fun addLevel(sender: CommandSender, args: Array<String>) {
        val playerExact = Bukkit.getPlayerExact(args[0])
        if (playerExact == null) {
            TLocale.sendTo(sender, "command-player-not-found")
            return
        }
        val option = getLevel(args[1])
        if (option == null) {
            TLocale.sendTo(sender, "command-level-not-found")
            return
        }
        val profile = playerExact.chemdahProfile
        val level = option.toLevel(profile.getLevel(args[1]))
        level.addLevel(Coerce.toInteger(args[2]))
        profile.setLevel(args[1], level.toPlayerLevel())
        sender.sendLocale("command-level-change", "§7${args[1]} (LEVEL) §8+= §f${args[2]} §7(Lv.${level.level}, ${level.experience})")
    }

    @SubCommand(
        description = "@command-level-set-level",
        arguments = ["@command-argument-player", "@command-argument-level", "@command-argument-value"],
        priority = 1.1
    )
    fun setLevel(sender: CommandSender, args: Array<String>) {
        val playerExact = Bukkit.getPlayerExact(args[0])
        if (playerExact == null) {
            TLocale.sendTo(sender, "command-player-not-found")
            return
        }
        val option = getLevel(args[1])
        if (option == null) {
            TLocale.sendTo(sender, "command-level-not-found")
            return
        }
        val profile = playerExact.chemdahProfile
        val level = option.toLevel(profile.getLevel(args[1]))
        level.setLevel(Coerce.toInteger(args[2]))
        profile.setLevel(args[1], level.toPlayerLevel())
        sender.sendLocale("command-level-change", "§7${args[1]} (LEVEL) §8= §f${args[2]} §7(Lv.${level.level}, ${level.experience})")
    }

    @SubCommand(
        description = "@command-level-add-exp",
        arguments = ["@command-argument-player", "@command-argument-level", "@command-argument-value"],
        priority = 1.2
    )
    fun addExp(sender: CommandSender, args: Array<String>) {
        val playerExact = Bukkit.getPlayerExact(args[0])
        if (playerExact == null) {
            TLocale.sendTo(sender, "command-player-not-found")
            return
        }
        val option = getLevel(args[1])
        if (option == null) {
            TLocale.sendTo(sender, "command-level-not-found")
            return
        }
        val profile = playerExact.chemdahProfile
        val level = option.toLevel(profile.getLevel(args[1]))
        level.addExperience(Coerce.toInteger(args[2]))
        profile.setLevel(args[1], level.toPlayerLevel())
        sender.sendLocale("command-level-change", "§7${args[1]} (EXP) §8+= §f${args[2]} §7(Lv.${level.level}, ${level.experience})")
    }

    @SubCommand(
        description = "@command-level-set-exp",
        arguments = ["@command-argument-player", "@command-argument-level", "@command-argument-value"],
        priority = 1.3
    )
    fun setExp(sender: CommandSender, args: Array<String>) {
        val playerExact = Bukkit.getPlayerExact(args[0])
        if (playerExact == null) {
            TLocale.sendTo(sender, "command-player-not-found")
            return
        }
        val option = getLevel(args[1])
        if (option == null) {
            TLocale.sendTo(sender, "command-level-not-found")
            return
        }
        val profile = playerExact.chemdahProfile
        val level = option.toLevel(profile.getLevel(args[1]))
        level.setExperience(Coerce.toInteger(args[2]))
        profile.setLevel(args[1], level.toPlayerLevel())
        sender.sendLocale("command-level-change", "§7${args[1]} (EXP) §8= §f${args[2]} §7(Lv.${level.level}, ${level.experience})")
    }
}