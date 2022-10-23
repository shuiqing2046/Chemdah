package ink.ptms.chemdah.module.command

import ink.ptms.chemdah.api.ChemdahAPI.chemdahProfile
import ink.ptms.chemdah.module.level.LevelSystem
import ink.ptms.chemdah.module.level.LevelSystem.getLevelOption
import ink.ptms.chemdah.module.level.LevelSystem.giveExperience
import ink.ptms.chemdah.module.level.LevelSystem.giveLevel
import ink.ptms.chemdah.module.level.LevelSystem.setExperience
import ink.ptms.chemdah.module.level.LevelSystem.setLevel
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.*
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
        dynamic(comment = "player") {
            suggestPlayers()
            // level
            dynamic(comment = "level") {
                suggestion<CommandSender> { _, _ -> LevelSystem.level.keys.toList() }
                // value
                dynamic(comment = "value") {
                    execute<CommandSender> { sender, ctx, argument ->
                        val option = getLevelOption(ctx.argument(-1))!!
                        val profile = ctx.player(-2).cast<Player>().chemdahProfile
                        profile.giveLevel(option, Coerce.toInteger(argument)).thenAccept {
                            sender.sendLang("command-level-change", "§7${option.id} (LEVEL) §8+= §f${argument} §7(Lv.${it.level}, ${it.experience})")
                        }
                    }
                }
            }
        }
    }

    @CommandBody
    val setlevel = subCommand {
        dynamic(comment = "player") {
            suggestPlayers()
            // level
            dynamic(comment = "level") {
                suggestion<CommandSender> { _, _ -> LevelSystem.level.keys.toList() }
                // value
                dynamic(comment = "value") {
                    execute<CommandSender> { sender, ctx, argument ->
                        val option = getLevelOption(ctx.argument(-1))!!
                        val profile = ctx.player(-2).cast<Player>().chemdahProfile
                        profile.setLevel(option, Coerce.toInteger(argument)).thenAccept {
                            sender.sendLang("command-level-change", "§7${option.id} (LEVEL) §8= §f${argument} §7(Lv.${it.level}, ${it.experience})")
                        }
                    }
                }
            }
        }
    }

    @CommandBody
    val addexp = subCommand {
        dynamic(comment = "player") {
            suggestPlayers()
            // level
            dynamic(comment = "level") {
                suggestion<CommandSender> { _, _ -> LevelSystem.level.keys.toList() }
                // value
                dynamic(comment = "value") {
                    execute<CommandSender> { sender, ctx, argument ->
                        val option = getLevelOption(ctx.argument(-1))!!
                        val profile = ctx.player(-2).cast<Player>().chemdahProfile
                        profile.giveExperience(option, Coerce.toInteger(argument)).thenAccept {
                            sender.sendLang("command-level-change", "§7${option.id} (EXP) §8+= §f${argument} §7(Lv.${it.level}, ${it.experience})")
                        }
                    }
                }
            }
        }
    }

    @CommandBody
    val setexp = subCommand {
        dynamic(comment = "player") {
            suggestPlayers()
            // level
            dynamic(comment = "level") {
                suggestion<CommandSender> { _, _ -> LevelSystem.level.keys.toList() }
                // value
                dynamic(comment = "value") {
                    execute<CommandSender> { sender, ctx, argument ->
                        val option = getLevelOption(ctx.argument(-1))!!
                        val profile = ctx.player(-2).cast<Player>().chemdahProfile
                        profile.setExperience(option, Coerce.toInteger(argument)).thenAccept {
                            sender.sendLang("command-level-change", "§7${option.id} (EXP) §8= §f${argument} §7(Lv.${it.level}, ${it.experience})")
                        }
                    }
                }
            }
        }
    }
}