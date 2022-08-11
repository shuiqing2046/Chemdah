package ink.ptms.chemdah.module.command

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.api.ChemdahAPI.chemdahProfile
import ink.ptms.chemdah.module.party.PartySystem
import ink.ptms.chemdah.module.party.PartySystem.getParty
import ink.ptms.chemdah.module.party.PartySystem.getPartyMembers
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.adaptCommandSender
import taboolib.common.platform.function.onlinePlayers
import taboolib.common.platform.function.submit
import taboolib.common5.Mirror
import taboolib.expansion.createHelper
import taboolib.platform.util.sendLang

/**
 * @author sky
 * @since 2021/2/11 7:19 下午
 */
@CommandHeader(name = "Chemdah", aliases = ["ch"], permission = "chemdah.command")
object CommandChemdah {

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

    @CommandBody(optional = true)
    val api = CommandChemdahAPI

    @CommandBody
    val data = CommandChemdahPlayerData

    @CommandBody
    val level = CommandChemdahPlayerLevel

    @CommandBody
    val quest = CommandChemdahQuest

    @CommandBody
    val script = CommandChemdahScript

    @CommandBody(aliases = ["vars"])
    val variable = CommandChemdahVariables

    @CommandBody
    val info = subCommand {
        dynamic(commit = "player") {
            suggestion<CommandSender> { _, _ -> onlinePlayers().map { it.name } }
            execute<CommandSender> { sender, _, argument ->
                val playerExact = Bukkit.getPlayerExact(argument)!!
                sender.sendLang("command-info-header")
                sender.sendLang("command-info-body", "  §7Data:")
                playerExact.chemdahProfile.persistentDataContainer.entries().sortedBy { it.key }.forEach { e ->
                    sender.sendLang("command-info-body", "    §7${e.key.replace(".", "§f.§7")} §8= §f${e.value.data}")
                }
                val quests = playerExact.chemdahProfile.getQuests(openAPI = true)
                sender.sendLang("command-info-body", "  §7Quests: §f${quests.filter { it.isOwner(playerExact) }.map { it.id }.toList()}")
                sender.sendLang("command-info-body", "  §7Quests (Share): §f${quests.filter { !it.isOwner(playerExact) }.map { it.id }.toList()}")
                val partyHook = PartySystem.hook?.javaClass?.name
                if (partyHook != null) {
                    sender.sendLang("command-info-body", "  §7Party: §f$partyHook")
                    val party = playerExact.getParty()
                    if (party != null) {
                        sender.sendLang("command-info-body", "  §7 = Leader: §f${party.isLeader(playerExact)}")
                        sender.sendLang("command-info-body", "  §7 = Member: §f${playerExact.getPartyMembers(true).map { it.name }}")
                    }
                }
            }
        }
    }

    @CommandBody
    val mirror = subCommand {
        execute<CommandSender> { sender, _, _ ->
            sender.sendMessage("§c[Chemdah] §7Mirror system is disabled, please use professional performance monitoring tools.")
            sender.sendMessage("§c[Chemdah] §7spark: §f§nhttps://spark.lucko.me")
            sender.sendMessage("§c[Chemdah] §7async-profiler: §f§nhttps://github.com/jvm-profiling-tools/async-profiler")
        }
    }

    @CommandBody
    val reload = subCommand {
        execute<CommandSender> { sender, _, _ ->
            ChemdahAPI.reloadAll()
            sender.sendLang("command-reload-success")
        }
    }
}