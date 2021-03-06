package ink.ptms.chemdah.api.event

import ink.ptms.chemdah.core.PlayerProfile
import io.izzel.taboolib.module.event.EventNormal
import org.bukkit.Bukkit
import org.bukkit.entity.Player

/**
 * Chemdah
 * ink.ptms.chemdah.api.event.ChemdahEvents
 *
 * @author sky
 * @since 2021/3/7 1:31 上午
 */
class ChemdahEvents {

    /**
     * 当玩家数据加载完成时
     */
    class Selected(val player: Player, val playerProfile: PlayerProfile) : EventNormal<Selected>() {

        init {
            async(!Bukkit.isPrimaryThread())
        }
    }
}