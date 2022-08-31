package ink.ptms.chemdah.module.compat

import ink.ptms.chemdah.api.ChemdahAPI.isChemdahProfileLoaded
import ink.ptms.chemdah.util.namespaceQuest
import org.bukkit.entity.Player
import taboolib.common.platform.function.adaptPlayer
import taboolib.module.kether.KetherShell
import taboolib.module.kether.printKetherErrorMessage
import taboolib.platform.compat.PlaceholderExpansion

/**
 * Chemdah
 * ink.ptms.chemdah.compat.CompatPlaceholderAPI
 *
 * @author sky
 * @since 2021/3/8 11:07 下午
 */
object CompatPlaceholderAPI : PlaceholderExpansion {

    override val identifier: String
        get() = "chemdah"

    override fun onPlaceholderRequest(player: Player?, args: String): String {
        player ?: return "<NO_PLAYER>"
        return if (player.isChemdahProfileLoaded) {
            try {
                KetherShell.eval(args, sender = adaptPlayer(player), namespace = namespaceQuest).getNow("<TIMEOUT>").toString()
            } catch (ex: Throwable) {
                ex.printKetherErrorMessage()
                "<ERROR>"
            }
        } else {
            "..."
        }
    }
}