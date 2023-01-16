package ink.ptms.chemdah.api.event.collect

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Task
import ink.ptms.chemdah.core.quest.Template
import ink.ptms.chemdah.module.level.LevelOption
import ink.ptms.chemdah.module.scenes.ScenesBlockData
import org.bukkit.entity.Player
import taboolib.platform.type.BukkitProxyEvent

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
    class Selected(val player: Player, val playerProfile: PlayerProfile) : BukkitProxyEvent() {

        override val allowCancelled: Boolean
            get() = false
    }

    /**
     * 当玩家离开服务器时
     * 包含 Quit 以及 Kick 事件
     */
    class Released(val player: Player) : BukkitProxyEvent() {

        override val allowCancelled: Boolean
            get() = false
    }

    /**
     * 当玩家数据更新时
     */
    class Updated(val player: Player, val playerProfile: PlayerProfile) : BukkitProxyEvent() {

        override val allowCancelled: Boolean
            get() = false
    }

    /**
     * 当玩家追踪任务时
     */
    class Track(val player: Player, val playerProfile: PlayerProfile, val trackingQuest: Template?, val cancel: Boolean) : BukkitProxyEvent()

    /**
     * 当玩家追踪条目时
     */
    class TrackTask(val player: Player, val playerProfile: PlayerProfile, val trackingTask: Task, val trackType: Type) : BukkitProxyEvent() {

        /** 追踪器类型 */
        enum class Type {

            BEACON, LANDMARK, NAVIGATION, SCOREBOARD
        }
    }

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
    ) : BukkitProxyEvent()

    /**
     * 当玩家破坏演出方块
     */
    class ScenesBlockBreak(val player: Player, val blockData: ScenesBlockData): BukkitProxyEvent() {

        init {
            isCancelled = true
        }
    }

    /**
     * 当玩家交互演出方块
     */
    class ScenesBlockInteract(val player: Player, val blockData: ScenesBlockData): BukkitProxyEvent() {

        override val allowCancelled: Boolean
            get() = false
    }
}