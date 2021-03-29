package ink.ptms.chemdah.api.event

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Template
import io.izzel.taboolib.module.event.EventCancellable
import io.izzel.taboolib.module.event.EventNormal
import org.bukkit.Bukkit
import org.bukkit.entity.Player

/**
 * Chemdah
 * ink.ptms.chemdah.api.event.PlayerEvent
 *
 * @author sky
 * @since 2021/3/7 1:31 上午
 */
class PlayerEvent {

    /**
     * 当玩家数据加载完成时
     */
    class Selected(val player: Player, val playerProfile: PlayerProfile) : EventNormal<Selected>() {

        init {
            async(!Bukkit.isPrimaryThread())
        }
    }

    /**
     * 当玩家数据更新时
     */
    class Updated(val player: Player, val playerProfile: PlayerProfile) : EventNormal<Updated>() {

        init {
            async(!Bukkit.isPrimaryThread())
        }
    }

    /**
     * 当玩家追踪任务时
     */
    class Track(val player: Player, val playerProfile: PlayerProfile, val template: Template): EventCancellable<Track>() {

        init {
            async(!Bukkit.isPrimaryThread())
        }
    }
}