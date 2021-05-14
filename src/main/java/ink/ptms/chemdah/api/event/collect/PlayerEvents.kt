package ink.ptms.chemdah.api.event.collect

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Template
import ink.ptms.chemdah.module.kether.ActionScenes
import ink.ptms.chemdah.module.level.LevelOption
import ink.ptms.chemdah.module.scenes.ScenesBlockData
import io.izzel.taboolib.module.event.EventCancellable
import io.izzel.taboolib.module.event.EventNormal
import org.bukkit.entity.Player

/**
 * Chemdah
 * ink.ptms.chemdah.api.event.collect.PlayerEvents
 *
 * @author sky
 * @since 2021/3/7 1:31 上午
 */
class PlayerEvents {

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

    /**
     * 当玩家破坏演出方块
     */
    class ScenesBlockBreak(val player: Player, val blockData: ScenesBlockData): EventCancellable<ScenesBlockBreak>(true) {

        init {
            isCancelled = true
        }
    }

    /**
     * 当玩家交互演出方块
     */
    class ScenesBlockInteract(val player: Player, val blockData: ScenesBlockData): EventNormal<ScenesBlockInteract>(true)
}