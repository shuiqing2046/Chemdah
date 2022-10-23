package ink.ptms.chemdah.module.command

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.api.ChemdahAPI.callTrigger
import ink.ptms.chemdah.api.ChemdahAPI.chemdahProfile
import ink.ptms.chemdah.api.ChemdahAPI.isChemdahProfileLoaded
import ink.ptms.chemdah.core.quest.addon.AddonTrack.Companion.trackQuest
import ink.ptms.chemdah.core.quest.objective.other.getAvailableTriggers
import ink.ptms.chemdah.module.ui.UISystem
import org.apache.commons.lang3.time.DateFormatUtils
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
        dynamic(comment = "player") {
            suggestPlayers()
            dynamic(comment = "quest") {
                suggestion<CommandSender> { _, _ -> ChemdahAPI.questTemplate.keys.toList() }
                execute<CommandSender> { _, ctx, argument ->
                    ChemdahAPI.getQuestTemplate(argument)!!.acceptTo(ctx.player(-1).cast<Player>().chemdahProfile)
                }
            }
        }
    }

    @CommandBody
    val failure = subCommand {
        dynamic(comment = "player") {
            suggestPlayers()
            dynamic(comment = "quest") {
                suggestion<CommandSender>(uncheck = true) { _, ctx ->
                    ctx.player(-1).cast<Player>().chemdahProfile.getQuests().map { it.id }
                }
                execute<CommandSender> { sender, ctx, argument ->
                    val profile = ctx.player(-1).cast<Player>().chemdahProfile
                    if (argument == "*") {
                        profile.getQuests(false).forEach { it.failQuestFuture() }
                    } else {
                        val quest = profile.getQuestById(argument, openAPI = false)
                        if (quest == null) {
                            sender.sendLang("command-quest-not-accepted")
                            return@execute
                        }
                        quest.failQuestFuture()
                    }
                }
            }
        }
    }

    @CommandBody
    val complete = subCommand {
        dynamic(comment = "player") {
            suggestPlayers()
            dynamic(comment = "quest") {
                suggestion<CommandSender>(uncheck = true) { _, ctx ->
                    ctx.player(-1).cast<Player>().chemdahProfile.getQuests().map { it.id }
                }
                execute<CommandSender> { sender, ctx, argument ->
                    val profile = ctx.player(-1).cast<Player>().chemdahProfile
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
        dynamic(comment = "player") {
            suggestPlayers()
            dynamic(comment = "quest") {
                suggestion<CommandSender>(uncheck = true) { _, ctx ->
                    ctx.player(-1).cast<Player>().chemdahProfile.getQuests().map { it.id }
                }
                execute<CommandSender> { sender, ctx, argument ->
                    val profile = ctx.player(-1).cast<Player>().chemdahProfile
                    if (argument == "*") {
                        profile.getQuests(false).forEach { it.restartQuestFuture() }
                    } else {
                        val quest = profile.getQuestById(argument, openAPI = false)
                        if (quest == null) {
                            sender.sendLang("command-quest-not-accepted")
                            return@execute
                        }
                        quest.restartQuestFuture()
                    }
                }
            }
        }
    }

    @CommandBody
    val stop = subCommand {
        dynamic(comment = "player") {
            suggestPlayers()
            dynamic(comment = "quest") {
                suggestion<CommandSender>(uncheck = true) { _, ctx ->
                    ctx.player(-1).cast<Player>().chemdahProfile.getQuests().map { it.id }
                }
                execute<CommandSender> { sender, ctx, argument ->
                    val profile = ctx.player(-1).cast<Player>().chemdahProfile
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
        dynamic(comment = "player") {
            suggestPlayers()
            dynamic(comment = "value") {
                suggestion<CommandSender>(uncheck = true) { _, ctx -> ctx.player(-1).cast<Player>().chemdahProfile.getAvailableTriggers() }
                execute<CommandSender> { _, ctx, argument ->
                    ctx.player(-1).cast<Player>().callTrigger(argument)
                }
            }
        }
    }

    @CommandBody
    val triggerAll = subCommand {
        dynamic(comment = "value") {
            suggestion<CommandSender>(uncheck = true) { _, _ ->
                Bukkit.getOnlinePlayers().filter { it.isChemdahProfileLoaded }.flatMap { it.chemdahProfile.getAvailableTriggers() }.toSet().toList()
            }
            execute<CommandSender> { _, _, argument ->
                Bukkit.getOnlinePlayers().forEach { it.callTrigger(argument) }
            }
        }
    }

    @CommandBody
    val info = subCommand {
        dynamic(comment = "player") {
            suggestPlayers()
            dynamic(comment = "quest", optional = true) {
                suggestion<CommandSender>(uncheck = true) { _, ctx ->
                    ctx.player(-1).cast<Player>().chemdahProfile.getQuests(openAPI = true).map { it.id }
                }
                execute<CommandSender> { sender, ctx, argument ->
                    if (Coerce.asInteger(argument).isPresent) {
                        commandInfo(sender, ctx.argument(-1), Coerce.toInteger(argument) - 1)
                    } else {
                        commandInfo(sender, ctx.argument(-1), argument)
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
        dynamic(comment = "player") {
            suggestPlayers()
            dynamic(comment = "ui") {
                suggestion<CommandSender> { _, _ -> UISystem.ui.keys.toList() }
                execute<CommandSender> { _, ctx, argument ->
                    UISystem.getUI(argument)!!.open(ctx.player(-1).cast<Player>().chemdahProfile)
                }
            }
        }
    }

    @CommandBody
    val track = subCommand {
        dynamic(comment = "player") {
            suggestPlayers()
            dynamic(comment = "quest") {
                suggestion<CommandSender> { _, _ -> ChemdahAPI.questTemplate.keys.toList() }
                execute<CommandSender> { _, ctx, argument ->
                    ctx.player(-1).cast<Player>().chemdahProfile.trackQuest = ChemdahAPI.getQuestTemplate(argument)
                }
            }
            literal("cancel") {
                execute<CommandSender> { _, ctx, _ ->
                    ctx.player(-1).cast<Player>().chemdahProfile.trackQuest = null
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
                // 任务名称
                if (quest.isOwner(playerExact)) {
                    sender.sendLang("command-quest-info-name", quest.id)
                } else {
                    sender.sendLang("command-quest-info-name-share", quest.id)
                }
                // 开始时间
                sender.sendLang("command-quest-info-start-at", DateFormatUtils.format(quest.startTime, "yyyy/MM/dd HH:mm:ss"))
                // 任务数据
                sender.sendLang("command-quest-info-data")
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
            // 任务名称
            if (quest.isOwner(playerExact)) {
                sender.sendLang("command-quest-info-name", quest.id)
            } else {
                sender.sendLang("command-quest-info-name-share", quest.id)
            }
            // 开始时间
            sender.sendLang("command-quest-info-start-at", DateFormatUtils.format(quest.startTime, "yyyy/MM/dd HH:mm:ss"))
            // 任务数据
            sender.sendLang("command-quest-info-data")
            quest.persistentDataContainer.entries().forEach { e ->
                sender.sendLang("command-quest-info-body", "      §7${e.key.replace(".", "§f.§7")} §8= §f${e.value.data}")
            }
        }
    }
}