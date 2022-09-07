package ink.ptms.chemdah.module.command

import ink.ptms.chemdah.api.ChemdahAPI.chemdahProfile
import ink.ptms.chemdah.module.level.LevelSystem
import ink.ptms.chemdah.module.level.LevelSystem.getLevel
import ink.ptms.chemdah.module.level.LevelSystem.getLevelOption
import ink.ptms.chemdah.module.level.LevelSystem.setLevel
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import taboolib.common.platform.command.*
import taboolib.common.platform.function.onlinePlayers
import taboolib.common5.Coerce
import taboolib.expansion.createHelper
import taboolib.platform.util.sendLang

/**
 * Chemdah
 * ink.ptms.chemdah.command.CommandChemdahPlayerLevel
 *
 * @author sky
 * @since 2021/2/11 7:19 下午
 */
@Suppress("SpellCheckingInspection")
@CommandHeader(name = "ChemdahPlayerLevel", aliases = ["chpl"], permission = "chemdah.command")
object CommandChemdahPlayerLevel {

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

    @CommandBody
    val addlevel = subCommand { 
        dynamic(commit = "player") {
            suggestPlayers()
            // level
            dynamic(commit = "level") {
                suggestion<CommandSender> { _, _ -> LevelSystem.level.keys.toList() }
                // value
                dynamic(commit = "value") {
                    execute<CommandSender> { sender, context, argument ->
                        val playerExact = Bukkit.getPlayerExact(context.argument(-2))!!
                        val option = getLevelOption(context.argument(-1))!!
                        val profile = playerExact.chemdahProfile
                        val level = option.toLevel(profile.getLevel(option))
                        level.addLevel(Coerce.toInteger(argument)).thenAccept {
                            profile.setLevel(option, level.toPlayerLevel())
                            profile.getLevel(option).also {
                                sender.sendLang("command-level-change", "§7${option.id} (LEVEL) §8+= §f${argument} §7(Lv.${it.level}, ${it.experience})")
                            }
                        }
                    }
                }
            }
        }
    }

    @CommandBody
    val setlevel = subCommand {
        dynamic(commit = "player") {
            suggestPlayers()
            // level
            dynamic(commit = "level") {
                suggestion<CommandSender> { _, _ -> LevelSystem.level.keys.toList() }
                // value
                dynamic(commit = "value") {
                    execute<CommandSender> { sender, context, argument ->
                        val playerExact = Bukkit.getPlayerExact(context.argument(-2))!!
                        val option = getLevelOption(context.argument(-1))!!
                        val profile = playerExact.chemdahProfile
                        val level = option.toLevel(profile.getLevel(option))
                        level.setLevel(Coerce.toInteger(argument)).thenAccept {
                            profile.setLevel(option, level.toPlayerLevel())
                            profile.getLevel(option).also {
                                sender.sendLang("command-level-change", "§7${option.id} (LEVEL) §8= §f${argument} §7(Lv.${it.level}, ${it.experience})")
                            }
                        }
                    }
                }
            }
        }
    }

    @CommandBody
    val addexp = subCommand {
        dynamic(commit = "player") {
            suggestPlayers()
            // level
            dynamic(commit = "level") {
                suggestion<CommandSender> { _, _ -> LevelSystem.level.keys.toList() }
                // value
                dynamic(commit = "value") {
                    execute<CommandSender> { sender, context, argument ->
                        val playerExact = Bukkit.getPlayerExact(context.argument(-2))!!
                        val option = getLevelOption(context.argument(-1))!!
                        val profile = playerExact.chemdahProfile
                        val level = option.toLevel(profile.getLevel(option))
                        level.addExperience(Coerce.toInteger(argument)).thenAccept {
                            profile.setLevel(option, level.toPlayerLevel())
                            profile.getLevel(option).also {
                                sender.sendLang("command-level-change", "§7${option.id} (EXP) §8+= §f${argument} §7(Lv.${it.level}, ${it.experience})")
                            }
                        }
                    }
                }
            }
        }
    }

    @CommandBody
    val setexp = subCommand {
        dynamic(commit = "player") {
            suggestPlayers()
            // level
            dynamic(commit = "level") {
                suggestion<CommandSender> { _, _ -> LevelSystem.level.keys.toList() }
                // value
                dynamic(commit = "value") {
                    execute<CommandSender> { sender, context, argument ->
                        val playerExact = Bukkit.getPlayerExact(context.argument(-2))!!
                        val option = getLevelOption(context.argument(-1))!!
                        val profile = playerExact.chemdahProfile
                        val level = option.toLevel(profile.getLevel(option))
                        level.setExperience(Coerce.toInteger(argument)).thenAccept {
                            profile.setLevel(option, level.toPlayerLevel())
                            profile.getLevel(option).also {
                                sender.sendLang("command-level-change", "§7${option.id} (EXP) §8= §f${argument} §7(Lv.${it.level}, ${it.experience})")
                            }
                        }
                    }
                }
            }
        }
    }
}