package ink.ptms.chemdah.module.command

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.api.ChemdahAPI.callTrigger
import ink.ptms.chemdah.api.ChemdahAPI.chemdahProfile
import ink.ptms.chemdah.core.quest.addon.AddonTrack.Companion.trackQuest
import ink.ptms.chemdah.module.ui.UISystem
import org.apache.commons.lang3.time.DateFormatUtils
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.adaptCommandSender
import taboolib.common.platform.function.onlinePlayers
import taboolib.common.util.subList
import taboolib.common5.Coerce
import taboolib.expansion.createHelper
import taboolib.module.chat.TellrawJson
import taboolib.platform.util.sendLang
import kotlin.math.ceil

/**
 * Chemdah
 * ink.ptms.chemdah.command.CommandChemdahQuest
 *
 * @author sky
 * @since 2021/2/11 7:19 下午
 */
@Suppress("DuplicatedCode")
@CommandHeader(name = "ChemdahQuest", aliases = ["chq"], permission = "chemdah.command")
object CommandChemdahQuest {

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

    @CommandBody
    val accept = subCommand {
        dynamic(commit = "player") {
            suggestion<CommandSender> { _, _ -> onlinePlayers().map { it.name } }
            dynamic(commit = "quest") {
                suggestion<CommandSender> { _, _ -> ChemdahAPI.questTemplate.keys.toList() }
                execute<CommandSender> { _, context, argument ->
                    val playerExact = Bukkit.getPlayerExact(context.argument(-1))!!
                    val quest = ChemdahAPI.getQuestTemplate(argument)!!
                    quest.acceptTo(playerExact.chemdahProfile)
                }
            }
        }
    }

    @CommandBody
    val failure = subCommand {
        dynamic(commit = "player") {
            suggestion<CommandSender> { _, _ -> onlinePlayers().map { it.name } }
            dynamic(commit = "quest") {
                suggestion<CommandSender>(uncheck = true) { _, _ -> ChemdahAPI.questTemplate.keys.toList() }
                execute<CommandSender> { sender, context, argument ->
                    val playerExact = Bukkit.getPlayerExact(context.argument(-1))!!
                    val profile = playerExact.chemdahProfile
                    if (argument == "*") {
                        profile.getQuests(false).forEach { it.failQuest() }
                    } else {
                        val quest = profile.getQuestById(argument, openAPI = false)
                        if (quest == null) {
                            sender.sendLang("command-quest-not-accepted")
                            return@execute
                        }
                        quest.failQuest()
                    }
                }
            }
        }
    }

    @CommandBody
    val complete = subCommand {
        dynamic(commit = "player") {
            suggestion<CommandSender> { _, _ -> onlinePlayers().map { it.name } }
            dynamic(commit = "quest") {
                suggestion<CommandSender>(uncheck = true) { _, _ -> ChemdahAPI.questTemplate.keys.toList() }
                execute<CommandSender> { sender, context, argument ->
                    val playerExact = Bukkit.getPlayerExact(context.argument(-1))!!
                    val profile = playerExact.chemdahProfile
                    if (argument == "*") {
                        profile.getQuests(false).forEach { it.completeQuest() }
                    } else {
                        val quest = profile.getQuestById(argument, openAPI = false)
                        if (quest == null) {
                            sender.sendLang("command-quest-not-accepted")
                            return@execute
                        }
                        quest.completeQuest()
                    }
                }
            }
        }
    }

    @CommandBody
    val restart = subCommand {
        dynamic(commit = "player") {
            suggestion<CommandSender> { _, _ -> onlinePlayers().map { it.name } }
            dynamic(commit = "quest") {
                suggestion<CommandSender>(uncheck = true) { _, _ -> ChemdahAPI.questTemplate.keys.toList() }
                execute<CommandSender> { sender, context, argument ->
                    val playerExact = Bukkit.getPlayerExact(context.argument(-1))!!
                    val profile = playerExact.chemdahProfile
                    if (argument == "*") {
                        profile.getQuests(false).forEach { it.restartQuest() }
                    } else {
                        val quest = profile.getQuestById(argument, openAPI = false)
                        if (quest == null) {
                            sender.sendLang("command-quest-not-accepted")
                            return@execute
                        }
                        quest.restartQuest()
                    }
                }
            }
        }
    }

    @CommandBody
    val stop = subCommand {
        dynamic(commit = "player") {
            suggestion<CommandSender> { _, _ -> onlinePlayers().map { it.name } }
            dynamic(commit = "quest") {
                suggestion<CommandSender>(uncheck = true) { _, _ -> ChemdahAPI.questTemplate.keys.toList() }
                execute<CommandSender> { sender, context, argument ->
                    val playerExact = Bukkit.getPlayerExact(context.argument(-1))!!
                    val profile = playerExact.chemdahProfile
                    if (argument == "*") {
                        profile.getQuests(false).forEach { profile.unregisterQuest(it) }
                    } else {
                        val quest = profile.getQuestById(argument, openAPI = false)
                        if (quest == null) {
                            sender.sendLang("command-quest-not-accepted")
                            return@execute
                        }
                        profile.unregisterQuest(quest)
                    }
                }
            }
        }
    }

    @CommandBody
    val trigger = subCommand {
        dynamic(commit = "player") {
            suggestion<CommandSender> { _, _ -> onlinePlayers().map { it.name } }
            dynamic(commit = "value") {
                execute<CommandSender> { _, context, argument ->
                    val playerExact = Bukkit.getPlayerExact(context.argument(-1))!!
                    playerExact.callTrigger(argument)
                }
            }
        }
    }

    @CommandBody
    val triggerAll = subCommand {
        dynamic(commit = "value") {
            execute<CommandSender> { _, _, argument ->
                Bukkit.getOnlinePlayers().forEach {
                    it.callTrigger(argument)
                }
            }
        }
    }

    @CommandBody
    val info = subCommand {
        dynamic(commit = "player") {
            suggestion<CommandSender> { _, _ -> onlinePlayers().map { it.name } }
            dynamic(commit = "quest", optional = true) {
                suggestion<CommandSender>(uncheck = true) { _, _ -> ChemdahAPI.questTemplate.keys.toList() }
                execute<CommandSender> { sender, context, argument ->
                    if (Coerce.asInteger(argument).isPresent) {
                        commandInfo(sender, context.argument(-1), Coerce.toInteger(argument) - 1)
                    } else {
                        commandInfo(sender, context.argument(-1), argument)
                    }
                }
            }
            execute<CommandSender> { sender, _, argument ->
                commandInfo(sender, argument, 0)
            }
        }
    }

    @CommandBody
    val ui = subCommand {
        dynamic(commit = "player") {
            suggestion<CommandSender> { _, _ -> onlinePlayers().map { it.name } }
            dynamic(commit = "ui") {
                suggestion<CommandSender> { _, _ -> UISystem.ui.keys.toList() }
                execute<CommandSender> { _, context, argument ->
                    val playerExact = Bukkit.getPlayerExact(context.argument(-1))!!
                    val ui = UISystem.getUI(argument)!!
                    ui.open(playerExact.chemdahProfile)
                }
            }
        }
    }

    @CommandBody
    val track = subCommand {
        dynamic(commit = "player") {
            suggestion<CommandSender> { _, _ -> onlinePlayers().map { it.name } }
            literal("cancel") {
                execute<CommandSender> { _, context, _ ->
                    val playerExact = Bukkit.getPlayerExact(context.argument(-1))!!
                    playerExact.chemdahProfile.trackQuest = null
                }
            }
            dynamic(commit = "quest") {
                suggestion<CommandSender> { _, _ -> ChemdahAPI.questTemplate.keys.toList() }
                execute<CommandSender> { _, context, argument ->
                    val playerExact = Bukkit.getPlayerExact(context.argument(-1))!!
                    val quest = ChemdahAPI.getQuestTemplate(argument)
                    playerExact.chemdahProfile.trackQuest = quest
                }
            }
        }
    }

    internal fun commandInfo(sender: CommandSender, player: String, page: Int = 0) {
        val playerExact = Bukkit.getPlayerExact(player)!!
        val quests = playerExact.chemdahProfile.getQuests(openAPI = true)
        if (quests.isEmpty()) {
            sender.sendLang("command-quest-info-empty")
            return
        } else {
            space(sender)
            sender.sendLang("command-quest-info-header")
            subList(quests, page * 3, (page + 1) * 3).forEach { quest ->
                sender.sendLang("command-quest-info-body", "  §n${quest.id}:§r ${if (!quest.isOwner(playerExact)) "§8(Share)" else ""}")
                sender.sendLang("command-quest-info-body", "    §7Start in ${DateFormatUtils.format(quest.startTime, "yyyy/MM/dd HH:mm:ss")}")
                sender.sendLang("command-quest-info-body", "    §7Data:")
                quest.persistentDataContainer.entries().forEach { e ->
                    sender.sendLang("command-quest-info-body", "      §7${e.key.replace(".", "§f.§7")} §8= §f${e.value.data}")
                }
            }
            val max = ceil(quests.size / 3.0).toInt()
            // 左侧不可翻页
            if (page == 0) {
                // 右侧可翻页
                if (max > 1) {
                    sender.sendLang("command-quest-info-bottom-0", 1, max, player, 2)
                }
            }
            // 右侧不可翻页
            else if (page + 1 == max) {
                sender.sendLang("command-quest-info-bottom-1", page + 1, max, player, page)
            }
            // 正常翻页
            else {
                sender.sendLang("command-quest-info-bottom-2", page + 1, max, player, page, page + 2)
            }
        }
    }

    internal fun commandInfo(sender: CommandSender, player: String, questName: String) {
        val playerExact = Bukkit.getPlayerExact(player)!!
        val quest = playerExact.chemdahProfile.getQuestById(questName, openAPI = true)
        if (quest == null) {
            sender.sendLang("command-quest-info-empty")
            return
        } else {
            space(sender)
            sender.sendLang("command-quest-info-header")
            sender.sendLang("command-quest-info-body", "  §n${quest.id}:§r ${if (!quest.isOwner(playerExact)) "§8(Share)" else ""}")
            sender.sendLang("command-quest-info-body", "    §7Start in ${DateFormatUtils.format(quest.startTime, "yyyy/MM/dd HH:mm:ss")}")
            sender.sendLang("command-quest-info-body", "    §7Data:")
            quest.persistentDataContainer.entries().forEach { e ->
                sender.sendLang("command-quest-info-body", "      §7${e.key.replace(".", "§f.§7")} §8= §f${e.value.data}")
            }
        }
    }

    internal fun space(sender: CommandSender) {
        TellrawJson().sendTo(adaptCommandSender(sender)) { repeat(30) { newLine() } }
    }
}