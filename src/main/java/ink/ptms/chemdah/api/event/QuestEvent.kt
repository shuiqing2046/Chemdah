package ink.ptms.chemdah.api.event

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.AgentType
import ink.ptms.chemdah.core.quest.Quest
import ink.ptms.chemdah.core.quest.QuestContainer
import ink.ptms.chemdah.core.quest.Template
import io.izzel.taboolib.module.event.EventCancellable
import io.izzel.taboolib.module.event.EventNormal
import org.bukkit.Bukkit

/**
 * Chemdah
 * ink.ptms.chemdah.api.event.QuestEvent
 *
 * @author sky
 * @since 2021/2/21 1:07 上午
 */
class QuestEvent {

    /**
     * 当任务中当脚本代理执行时
     */
    class Agent(val questContainer: QuestContainer, val playerProfile: PlayerProfile, val agentType: AgentType): EventCancellable<Agent>() {

        init {
            async(!Bukkit.isPrimaryThread())
        }
    }

    /**
     * 当任务进行接受检测时
     */
    class AcceptCheck(val template: Template, val playerProfile: PlayerProfile): EventCancellable<AcceptCheck>() {

        init {
            async(!Bukkit.isPrimaryThread())
        }
    }

    /**
     * 当任务接受后
     */
    class Accepted(val quest: Quest, val playerProfile: PlayerProfile): EventNormal<Accepted>() {

        init {
            async(!Bukkit.isPrimaryThread())
        }
    }

    /**
     * 当任务完成时
     */
    class Complete(val quest: Quest, val playerProfile: PlayerProfile): EventCancellable<Complete>() {

        init {
            async(!Bukkit.isPrimaryThread())
        }
    }

    /**
     * 当任务失败（放弃）时
     */
    class Failure(val quest: Quest, val playerProfile: PlayerProfile): EventCancellable<Failure>() {

        init {
            async(!Bukkit.isPrimaryThread())
        }
    }

    /**
     * 当任务重置时
     */
    class Reset(val quest: Quest, val playerProfile: PlayerProfile): EventCancellable<Reset>() {

        init {
            async(!Bukkit.isPrimaryThread())
        }
    }

    /**
     * 当任务被注册到玩家数据
     */
    class Registered(val quest: Quest, val playerProfile: PlayerProfile): EventNormal<Registered>() {

        init {
            async(!Bukkit.isPrimaryThread())
        }
    }

    /**
     * 当任务从玩家数据中注销
     */
    class Unregistered(val quest: Quest, val playerProfile: PlayerProfile): EventNormal<Unregistered>() {

        init {
            async(!Bukkit.isPrimaryThread())
        }
    }
}