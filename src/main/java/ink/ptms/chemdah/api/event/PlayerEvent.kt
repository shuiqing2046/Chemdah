package ink.ptms.chemdah.api.event

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Template
import ink.ptms.chemdah.module.level.LevelOption
import io.izzel.taboolib.module.event.EventCancellable
import io.izzel.taboolib.module.event.EventNormal
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
    class Selected(val player: Player, val playerProfile: PlayerProfile) : EventNormal<Selected>(true)

    /**
     * 当玩家数据更新时
     */
    class Updated(val player: Player, val playerProfile: PlayerProfile) : EventNormal<Updated>(true)

    /**
     * 当玩家追踪任务时
     */
    class Track(val player: Player, val playerProfile: PlayerProfile, val trackingQuest: Template?, val cancel: Boolean) : EventCancellable<Track>(true)

    /**
     * 当玩家的自定义等级数据发生变动
     */
    class LevelChange(
        val player: Player,
        val option: LevelOption,
        val oldLevel: Int,
        val oldExperience: Int,
        var newLevel: Int,
        var newExperience: Int,
    ) : EventCancellable<LevelChange>(true)
}