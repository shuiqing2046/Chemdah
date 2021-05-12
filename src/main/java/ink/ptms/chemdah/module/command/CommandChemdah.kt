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
@BaseCommand(name = "Chemdah", aliases = ["ch"], permission = "chemdah.command")
class CommandChemdah : BaseMainCommand() {

    @SubCommand(description = "@command-info", arguments = ["@command-argument-player"], priority = 1.0)
    fun info(sender: CommandSender, args: Array<String>) {
        val playerExact = Bukkit.getPlayerExact(args[0])
        if (playerExact == null) {
            TLocale.sendTo(sender, "command-player-not-found")
            return
        }
        TLocale.sendTo(sender, "command-info-header")
        TLocale.sendTo(sender, "command-info-body", "  §7Data:")
        playerExact.chemdahProfile.persistentDataContainer.entries().forEach { e ->
            TLocale.sendTo(sender, "command-info-body", "    §7${e.key.replace(".", "§f.§7")} §8= §f${e.value.data}")
        }
        val quests = playerExact.chemdahProfile.getQuests(openAPI = true)
        TLocale.sendTo(sender, "command-info-body", "  §7Quests: §f${quests.filter { it.isOwner(playerExact) }.map { it.id }.toList()}")
        TLocale.sendTo(sender, "command-info-body", "  §7Quests (Share): §f${quests.filter { !it.isOwner(playerExact) }.map { it.id }.toList()}")
    }

    @SubCommand(description = "@command-mirror", arguments = ["@command-argument-reset?"], priority = 1.1)
    fun mirror(sender: CommandSender, args: Array<String>) {
        if (Coerce.toBoolean(args.getOrNull(0) ?: false)) {
            mirror.dataMap.clear()
            sender.sendLocale("command-mirror-reset")
        } else {
            TLocale.sendTo(sender, "command-mirror-header")
            TLocale.sendTo(sender, "command-mirror-bottom")
            Tasks.task(true) {
                mirror.collectAndReport(sender)
                TLocale.sendTo(sender, "command-mirror-bottom")
            }
        }
    }

    @SubCommand(description = "@command-reload", priority = 9.0)
    fun reload(sender: CommandSender, args: Array<String>) {
        ChemdahAPI.reloadAll()
        TLocale.sendTo(sender, "command-reload-success")
    }
}