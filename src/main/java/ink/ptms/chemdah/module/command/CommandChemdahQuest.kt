package ink.ptms.chemdah.module.command

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.api.ChemdahAPI.callTrigger
import ink.ptms.chemdah.api.ChemdahAPI.chemdahProfile
import ink.ptms.chemdah.core.quest.addon.AddonTrack.Companion.trackQuest
import ink.ptms.chemdah.module.ui.UISystem
import io.izzel.taboolib.internal.apache.lang3.time.DateFormatUtils
import io.izzel.taboolib.kotlin.Indexed
import io.izzel.taboolib.module.command.base.BaseCommand
import io.izzel.taboolib.module.command.base.BaseMainCommand
import io.izzel.taboolib.module.command.base.SubCommand
import io.izzel.taboolib.module.locale.TLocale
import io.izzel.taboolib.util.Coerce
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import kotlin.math.ceil

/**
 * Chemdah
 * ink.ptms.chemdah.command.CommandChemdahQuest
 *
 * @author sky
 * @since 2021/2/11 7:19 下午
 */
@BaseCommand(name = "ChemdahQuest", aliases = ["chq"], permission = "chemdah.command")
class CommandChemdahQuest : BaseMainCommand() {

    override fun onTabComplete(sender: CommandSender, command: String, argument: String): List<String>? {
        return when (argument) {
            "@command-argument-quest" -> ChemdahAPI.questTemplate.keys.toList()
            "@command-argument-ui" -> UISystem.ui.keys.toList()
            else -> null
        }
    }

    @SubCommand(description = "@command-quest-accept", arguments = ["@command-argument-player", "@command-argument-quest"], priority = 1.0)
    fun accept(sender: CommandSender, args: Array<String>) {
        val playerExact = Bukkit.getPlayerExact(args[0])
        if (playerExact == null) {
            TLocale.sendTo(sender, "command-player-not-found")
            return
        }
        val quest = ChemdahAPI.getQuestTemplate(args[1])
        if (quest == null) {
            TLocale.sendTo(sender, "command-quest-not-found")
            return
        }
        quest.acceptTo(playerExact.chemdahProfile)
    }

    @SubCommand(description = "@command-quest-failure", arguments = ["@command-argument-player", "@command-argument-quest"], priority = 1.1)
    fun failure(sender: CommandSender, args: Array<String>) {
        val playerExact = Bukkit.getPlayerExact(args[0])
        if (playerExact == null) {
            TLocale.sendTo(sender, "command-player-not-found")
            return
        }
        val quest = playerExact.chemdahProfile.getQuestById(args[1], openAPI = false)
        if (quest == null) {
            TLocale.sendTo(sender, "command-quest-not-accepted")
            return
        }
        quest.failureQuest()
    }

    @SubCommand(description = "@command-quest-complete", arguments = ["@command-argument-player", "@command-argument-quest"], priority = 1.2)
    fun complete(sender: CommandSender, args: Array<String>) {
        val playerExact = Bukkit.getPlayerExact(args[0])
        if (playerExact == null) {
            TLocale.sendTo(sender, "command-player-not-found")
            return
        }
        val quest = playerExact.chemdahProfile.getQuestById(args[1], openAPI = false)
        if (quest == null) {
            TLocale.sendTo(sender, "command-quest-not-accepted")
            return
        }
        quest.completeQuest()
    }

    @SubCommand(description = "@command-quest-reset", arguments = ["@command-argument-player", "@command-argument-quest"], priority = 1.3)
    fun reset(sender: CommandSender, args: Array<String>) {
        val playerExact = Bukkit.getPlayerExact(args[0])
        if (playerExact == null) {
            TLocale.sendTo(sender, "command-player-not-found")
            return
        }
        val quest = playerExact.chemdahProfile.getQuestById(args[1], openAPI = false)
        if (quest == null) {
            TLocale.sendTo(sender, "command-quest-not-accepted")
            return
        }
        quest.resetQuest()
    }

    @SubCommand(description = "@command-quest-stop", arguments = ["@command-argument-player", "@command-argument-quest"], priority = 1.4)
    fun stop(sender: CommandSender, args: Array<String>) {
        val playerExact = Bukkit.getPlayerExact(args[0])
        if (playerExact == null) {
            TLocale.sendTo(sender, "command-player-not-found")
            return
        }
        val quest = playerExact.chemdahProfile.getQuestById(args[1], openAPI = false)
        if (quest == null) {
            TLocale.sendTo(sender, "command-quest-not-accepted")
            return
        }
        playerExact.chemdahProfile.unregisterQuest(quest)
    }

    @SubCommand(description = "@command-quest-trigger", arguments = ["@command-argument-player", "@command-argument-value"], priority = 1.41)
    fun trigger(sender: CommandSender, args: Array<String>) {
        val playerExact = Bukkit.getPlayerExact(args[0])
        if (playerExact == null) {
            TLocale.sendTo(sender, "command-player-not-found")
            return
        }
        playerExact.callTrigger(args[1])
    }

    @SubCommand(description = "@command-quest-trigger-all", arguments = ["@command-argument-value"], priority = 1.42)
    fun triggerAll(sender: CommandSender, args: Array<String>) {
        Bukkit.getOnlinePlayers().forEach {
            it.callTrigger(args[0])
        }
    }

    @SubCommand(description = "@command-quest-info", arguments = ["@command-argument-player", "@command-argument-page?"], priority = 1.5)
    fun info(sender: CommandSender, args: Array<String>) {
        val playerExact = Bukkit.getPlayerExact(args[0])
        if (playerExact == null) {
            TLocale.sendTo(sender, "command-player-not-found")
            return
        }
        val quests = playerExact.chemdahProfile.getQuests(openAPI = true)
        if (quests.isEmpty()) {
            TLocale.sendTo(sender, "command-quest-info-empty")
            return
        } else {
            TLocale.sendTo(sender, "command-quest-info-header")
            val page = Coerce.toInteger(args.getOrNull(2) ?: 0)
            Indexed.subList(quests, page * 5, (page + 1) * 5 - 1).forEach { quest ->
                TLocale.sendTo(sender, "command-quest-info-body", "  §n${quest.id}:§r ${if (!quest.isOwner(playerExact)) "§8(Share)" else ""}")
                TLocale.sendTo(sender, "command-quest-info-body", "    §7Start in ${DateFormatUtils.format(quest.startTime, "yyyy/MM/dd HH:mm:ss")}")
                TLocale.sendTo(sender, "command-quest-info-body", "    §7Data:")
                quest.persistentDataContainer.entries().forEach { e ->
                    TLocale.sendTo(sender, "command-quest-info-body", "      §7${e.key.replace(".", "§f.§7")} §8= §f${e.value.value}")
                }
            }
            TLocale.sendTo(sender, "command-quest-info-bottom", (page + 1), ceil(quests.size / 5.0).toInt())
        }
    }

    @SubCommand(description = "@command-quest-ui", arguments = ["@command-argument-player", "@command-argument-ui"], priority = 1.6)
    fun ui(sender: CommandSender, args: Array<String>) {
        val playerExact = Bukkit.getPlayerExact(args[0])
        if (playerExact == null) {
            TLocale.sendTo(sender, "command-player-not-found")
            return
        }
        val ui = UISystem.getUI(args[1])
        if (ui == null) {
            TLocale.sendTo(sender, "command-ui-not-found")
            return
        }
        ui.open(playerExact.chemdahProfile)
    }

    @SubCommand(description = "@command-quest-track", arguments = ["@command-argument-player", "@command-argument-quest"], priority = 1.7)
    fun track(sender: CommandSender, args: Array<String>) {
        val playerExact = Bukkit.getPlayerExact(args[0])
        if (playerExact == null) {
            TLocale.sendTo(sender, "command-player-not-found")
            return
        }
        val quest = ChemdahAPI.getQuestTemplate(args[1])
        if (quest == null) {
            TLocale.sendTo(sender, "command-quest-not-found")
            return
        }
        playerExact.chemdahProfile.trackQuest = quest
    }
}