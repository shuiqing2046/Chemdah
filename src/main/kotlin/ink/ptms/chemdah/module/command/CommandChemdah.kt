package ink.ptms.chemdah.module.command

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.api.ChemdahAPI.chemdahProfile
import ink.ptms.chemdah.module.party.PartySystem
import ink.ptms.chemdah.module.party.PartySystem.getParty
import ink.ptms.chemdah.module.party.PartySystem.getPartyMembers
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.*
import taboolib.common.util.subList
import taboolib.common5.Coerce
import taboolib.expansion.createHelper
import taboolib.platform.util.sendLang
import kotlin.math.ceil

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
        dynamic(comment ="player") {
            suggestPlayers()
            execute<CommandSender> { sender, _, argument ->
                commandInfo(sender, Bukkit.getPlayerExact(argument)!!)
            }
            dynamic(comment ="page", optional = true) {
                execute<CommandSender> { sender, ctx, argument ->
                    commandInfo(sender, ctx.player(-1).cast(), Coerce.toInteger(argument) - 1)
                }
            }
        }
    }

    @CommandBody
    val mirror = subCommand {
        execute<CommandSender> { sender, _, _ ->
            sender.sendMessage("§c[Chemdah] §7Mirror system is disabled.")
            sender.sendMessage("§c[Chemdah] §7Please use professional performance monitoring tools.")
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

    internal fun commandInfo(sender: CommandSender, player: Player, page: Int = 0) {
        space(sender)
        sender.sendLang("command-info-header")
        var show = false
        var showVars = false
        // 玩家普通任务
        val quests = player.chemdahProfile.getQuests(openAPI = true)
        val questsGeneric = quests.filter { it.isOwner(player) }.map { it.id }
        if (questsGeneric.isNotEmpty()) {
            show = true
            sender.sendLang("command-info-quest", questsGeneric.size, questsGeneric)
        }
        // 玩家共享任务
        val questsShare = quests.filter { !it.isOwner(player) }.map { it.id }
        if (questsShare.isNotEmpty()) {
            show = true
            sender.sendLang("command-info-quest-share", questsShare.size, questsShare)
        }
        // 组队信息
        val partyHook = PartySystem.hook?.javaClass?.name
        if (partyHook != null) {
            show = true
            sender.sendLang("command-info-party", partyHook)
            val party = player.getParty()
            if (party != null) {
                sender.sendLang("command-info-party-leader", party.isLeader(player))
                sender.sendLang("command-info-party-member", player.getPartyMembers(true).map { it.name })
            }
        }
        // 玩家数据
        if (player.chemdahProfile.persistentDataContainer.isNotEmpty()) {
            show = true
            showVars = true
            sender.sendLang("command-info-data")
            val vars = player.chemdahProfile.persistentDataContainer.entries().sortedBy { it.key }
            subList(vars, page * 12, (page + 1) * 12).forEach {
                sender.sendLang("command-info-body", "    §7${it.key.replace(".", "§f.§7")} §8= §f${it.value.data}")
            }
        }
        // 无数据
        if (!show) {
            sender.sendLang("command-info-empty")
        }
        // 显示变量翻页
        if (showVars) {
            val vars = player.chemdahProfile.persistentDataContainer.entries()
            val max = ceil(vars.size / 12.0).toInt()
            // 左侧不可翻页
            if (page == 0) {
                // 右侧可翻页
                if (max > 1) {
                    sender.sendLang("command-info-bottom-0", 1, max, player.name, 2)
                }
            }
            // 右侧不可翻页
            else if (page + 1 == max) {
                sender.sendLang("command-info-bottom-1", page + 1, max, player.name, page)
            }
            // 正常翻页
            else {
                sender.sendLang("command-info-bottom-2", page + 1, max, player.name, page, page + 2)
            }
        }
    }
}