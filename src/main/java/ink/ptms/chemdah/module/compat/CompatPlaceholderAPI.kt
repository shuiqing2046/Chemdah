package ink.ptms.chemdah.module.compat

import ink.ptms.chemdah.Chemdah
import ink.ptms.chemdah.api.ChemdahAPI.isChemdahProfileLoaded
import io.izzel.taboolib.kotlin.kether.KetherFunction
import io.izzel.taboolib.module.compat.PlaceholderHook
import io.izzel.taboolib.module.inject.THook
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

/**
 * Chemdah
 * ink.ptms.chemdah.compat.CompatPlaceholderAPI
 *
 * @author sky
 * @since 2021/3/8 11:07 下午
 */
@THook
class CompatPlaceholderAPI : PlaceholderHook.Expansion {

    override fun plugin(): Plugin {
        return Chemdah.plugin
    }

    override fun identifier(): String {
        return "chemdah"
    }

    override fun onPlaceholderRequest(player: Player, args: String): String {
        return if (player.isChemdahProfileLoaded) {
            KetherFunction.parse("{{ $args }}", namespace = listOf("chemdah")) {
                sender = player
            }
        } else {
            "..."
        }
    }
}