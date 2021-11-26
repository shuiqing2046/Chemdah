package ink.ptms.chemdah.module.compat

import ink.ptms.chemdah.api.ChemdahAPI.isChemdahProfileLoaded
import ink.ptms.chemdah.util.namespaceQuest
import org.bukkit.entity.Player
import taboolib.common.platform.function.adaptPlayer
import taboolib.module.kether.KetherFunction
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
        player ?: return "no player"
        return if (player.isChemdahProfileLoaded) {
            KetherFunction.parse("{{ $args }}", sender = adaptPlayer(player), namespace = namespaceQuest)
        } else {
            "..."
        }
    }
}