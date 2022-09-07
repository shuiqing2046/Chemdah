package ink.ptms.chemdah.module.command

import org.bukkit.command.CommandSender
import taboolib.common.platform.function.adaptCommandSender
import taboolib.module.chat.TellrawJson

internal fun space(sender: CommandSender) {
    TellrawJson().sendTo(adaptCommandSender(sender)) { repeat(30) { newLine() } }
}