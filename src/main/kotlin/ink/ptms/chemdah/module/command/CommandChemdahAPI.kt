package ink.ptms.chemdah.module.command

import ink.ptms.adyeshach.api.AdyeshachAPI
import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.core.bukkit.NMS
import ink.ptms.chemdah.core.conversation.trigger.TriggerAdyeshach.openConversation
import ink.ptms.chemdah.module.generator.NameGenerator
import ink.ptms.chemdah.module.scenes.ScenesSystem
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.*
import taboolib.common.platform.function.adaptCommandSender
import taboolib.common5.Coerce
import taboolib.expansion.createHelper
import taboolib.module.chat.TellrawJson
import taboolib.module.chat.colored
import taboolib.platform.util.sendLang

/**
 * Chemdah
 * ink.ptms.chemdah.command.ChemdahCommand
 *
 * @author sky
 * @since 2021/2/11 7:19 下午
 */
@Suppress("SpellCheckingInspection")
@CommandHeader(name = "ChemdahAPI", aliases = ["chapi", "cha"], permission = "chemdah.command")
object CommandChemdahAPI {

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

    @CommandBody
    val createscenes = subCommand {
        dynamic(comment = "player") {
            suggestPlayers()
            dynamic(comment = "scenes") {
                suggestion<CommandSender> { _, _ -> ScenesSystem.scenesMap.keys.toMutableList() }
                dynamic(comment = "index") {
                    execute<CommandSender> { sender, ctx, argument ->
                        val scenesFile = ScenesSystem.scenesMap[ctx.argument(-1)]
                        if (scenesFile == null) {
                            sender.sendLang("command-scenes-file-not-found")
                            return@execute
                        }
                        scenesFile.state.firstOrNull { it.index == Coerce.toInteger(argument) }?.send(ctx.player(-2).cast())
                    }
                }
            }
        }
    }

    @CommandBody
    val cancelscenes = subCommand {
        dynamic(comment = "player") {
            suggestPlayers()
            dynamic(comment = "scenes") {
                suggestion<CommandSender> { _, _ -> ScenesSystem.scenesMap.keys.toMutableList() }
                dynamic(comment = "index") {
                    execute<CommandSender> { sender, ctx, argument ->
                        val scenesFile = ScenesSystem.scenesMap[ctx.argument(-1)]
                        if (scenesFile == null) {
                            sender.sendLang("command-scenes-file-not-found")
                            return@execute
                        }
                        scenesFile.state.firstOrNull { it.index == Coerce.toInteger(argument) }?.cancel(ctx.player(-2).cast())
                    }
                }
            }
        }
    }

    @CommandBody
    val conversation = subCommand {
        literal("npc") {
            dynamic(comment = "player") {
                suggestPlayers()
                dynamic(comment = "id") {
                    suggestion<CommandSender> { _, ctx ->
                        AdyeshachAPI.getVisibleEntities(ctx.player(-1).cast()).map { it.id }
                    }
                    execute<CommandSender> { sender, ctx, argument ->
                        val player = ctx.player(-1).cast<Player>()
                        val npc = AdyeshachAPI.getVisibleEntities(player).firstOrNull { it.id == argument }
                        if (npc == null) {
                            sender.sendLang("command-adyeshach-not-found")
                            return@execute
                        }
                        npc.openConversation(player)
                    }
                }
            }
        }
        literal("self") {
            dynamic(comment = "player") {
                suggestPlayers()
                dynamic(comment = "id") {
                    suggestion<CommandSender> { _, _ -> ChemdahAPI.conversation.keys.toList() }
                    dynamic(comment = "name") {
                        execute<CommandSender> { sender, ctx, argument ->
                            val conversation = ChemdahAPI.conversation[ctx.argument(-1)]
                            if (conversation == null) {
                                sender.sendLang("command-conversation-not-found")
                                return@execute
                            }
                            conversation.openSelf(ctx.player(-2).cast(), argument.colored())
                        }
                    }
                }
            }
        }
    }

    @CommandBody
    val generate = subCommand {
        dynamic(comment = "name") {
            suggest { NameGenerator.generatorNames() }
            dynamic(comment = "amount", optional = true) {
                restrictInt()
                execute<CommandSender> { sender, ctx, _ ->
                    val names = NameGenerator.generate(ctx.argument(-1), ctx.int(0))
                    sender.sendLang("command-name-generated")
                    TellrawJson().sendTo(adaptCommandSender(sender)) {
                        names.forEach { name ->
                            append("&c[Chemdah] &8- ".colored())
                            append("&f$name".colored()).suggestCommand(name).hoverText("&7Click to copy".colored())
                            newLine()
                        }
                    }
                }
            }
            execute<CommandSender> { sender, ctx, _ ->
                val name = NameGenerator.generate(ctx.argument(0)).firstOrNull() ?: "null"
                sender.sendLang("command-name-generated")
                TellrawJson().sendTo(adaptCommandSender(sender)) {
                    append("&c[Chemdah] &8- ".colored())
                    append("&f$name".colored()).suggestCommand(name).hoverText("&7Click to copy".colored())
                    newLine()
                }
            }
        }
    }

    @CommandBody
    val blockinfo = subCommand {
        execute<Player> { sender, _, _ ->
            val block = sender.getTargetBlock(setOf(Material.AIR), 16)
            if (block.type.isAir) {
                sender.sendLang("command-block-info-is-air")
                return@execute
            }
            sender.sendLang("command-block-info-header")
            TellrawJson().sendTo(adaptCommandSender(sender)) {
                // 原版
                append("&c[Chemdah] ".colored())
                append("&8- &f${block.type.name.lowercase()}".colored())
                    .hoverText("&7Click to copy".colored())
                    .suggestCommand(block.type.name.lowercase())
                    .newLine()
                // 附加值
                val blocKData = NMS.INSTANCE.getBlocKData(block)
                if (blocKData.isNotEmpty()) {
                    val info = "${block.type.name.lowercase()}[${blocKData.entries.joinToString(",") { "${it.key}=${it.value}" }}]"
                    append("&c[Chemdah] ".colored())
                    append("&8- &f$info".colored())
                        .hoverText("&7Click to copy".colored())
                        .suggestCommand(info)
                }
            }
        }
    }

    @CommandBody(aliases = ["pos"])
    val position = subCommand {
        execute<Player> { sender, _, _ ->
            val loc = sender.location
            val x = Coerce.format(loc.x)
            val y = Coerce.format(loc.y)
            val z = Coerce.format(loc.z)
            sender.sendLang("command-position-header")
            TellrawJson().sendTo(adaptCommandSender(sender)) {
                // x,y,z
                append("&c[Chemdah] ".colored())
                append("&8- &f$x,$y,$z".colored())
                    .hoverText("&7Click to copy".colored())
                    .suggestCommand("$x,$y,$z")
                    .append(" ")
                append("&7(${x.toInt()},${y.toInt()},${z.toInt()})".colored())
                    .hoverText("&7Click to copy".colored())
                    .suggestCommand("${x.toInt()},${y.toInt()},${z.toInt()}")
                    .newLine()

                // x=?,y=?,z=?
                append("&c[Chemdah] ".colored())
                append("&8- &fx=$x,y=$y,z=$z".colored())
                    .hoverText("&7Click to copy".colored())
                    .suggestCommand("x=$x,y=$y,z=$z")
                    .append(" ")
                append("&7(x=${x.toInt()},y=${y.toInt()},z=${z.toInt()})".colored())
                    .hoverText("&7Click to copy".colored())
                    .suggestCommand("x=${x.toInt()},y=${y.toInt()},z=${z.toInt()}")
                    .newLine()

                // x y z
                append("&c[Chemdah] ".colored())
                append("&8- &f$x $y $z".colored())
                    .hoverText("&7Click to copy".colored())
                    .suggestCommand("$x $y $z")
                    .append(" ")
                append("&7(${x.toInt()} ${y.toInt()} ${z.toInt()})".colored())
                    .hoverText("&7Click to copy".colored())
                    .suggestCommand("${x.toInt()} ${y.toInt()} ${z.toInt()}")
                    .newLine()

                // x to ? y to ? z to ?
                append("&c[Chemdah] ".colored())
                append("&8- &fx to $x y to $y z to $z".colored())
                    .hoverText("&7Click to copy".colored())
                    .suggestCommand("x to $x y to $y z to $z")
                    .append(" ")
                append("&7(x to ${x.toInt()} y to ${y.toInt()} z to ${z.toInt()})".colored())
                    .hoverText("&7Click to copy".colored())
                    .suggestCommand("x to ${x.toInt()} y to ${y.toInt()} z to ${z.toInt()}")
                    .newLine()
            }
        }
    }
}