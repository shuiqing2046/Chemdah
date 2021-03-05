package ink.ptms.chemdah.command

import ink.ptms.chemdah.api.ChemdahAPI
import io.izzel.taboolib.module.command.base.BaseCommand
import io.izzel.taboolib.module.command.base.BaseMainCommand
import io.izzel.taboolib.module.command.base.SubCommand
import org.bukkit.command.CommandSender

/**
 * Chemdah
 * ink.ptms.chemdah.command.ChemdahCommand
 *
 * @author sky
 * @since 2021/2/11 7:19 下午
 */
@BaseCommand(name = "chemdah", aliases = ["ch"], permission = "chemdah.command")
class ChemdahCommand : BaseMainCommand() {

    @SubCommand(description = "重载配置文件", priority = 1.0)
    fun reload(sender: CommandSender, args: Array<String>) {
        ChemdahAPI.reloadAll()
        sender.sendMessage("§c[Chemdah] §7Successful.")
    }
}