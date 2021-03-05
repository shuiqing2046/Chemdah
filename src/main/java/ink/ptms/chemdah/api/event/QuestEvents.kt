package ink.ptms.chemdah.api.event

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.AgentType
import ink.ptms.chemdah.core.quest.QuestContainer
import ink.ptms.chemdah.core.quest.Template
import io.izzel.taboolib.module.event.EventCancellable
import io.izzel.taboolib.module.event.EventNormal
import org.bukkit.Bukkit

/**
 * Chemdah
 * ink.ptms.chemdah.api.event.QuestEvents
 *
 * @author sky
 * @since 2021/2/21 1:07 上午
 */
class QuestEvents {

    /**
     * 当任务中当脚本代理执行时
     */
    class Agent(val questContainer: QuestContainer, val playerProfile: PlayerProfile, val agentType: AgentType): EventCancellable<Agent>() {

        init {
            async(!Bukkit.isPrimaryThread())
        }
    }

    /**
     * 当任务接受时
     */
    class Accept(val template: Template, val playerProfile: PlayerProfile): EventCancellable<Accept>() {

        init {
            async(!Bukkit.isPrimaryThread())
        }
    }

    /**
     * 当任务接受后
     */
    class Accepted(val template: Template, val playerProfile: PlayerProfile): EventNormal<Accepted>() {

        init {
            async(!Bukkit.isPrimaryThread())
        }
    }

    /**
     * 当任务完成时
     */
    class Complete(val template: Template, val playerProfile: PlayerProfile): EventCancellable<Complete>() {

        init {
            async(!Bukkit.isPrimaryThread())
        }
    }

    /**
     * 当任务失败（放弃）时
     */
    class Failure(val template: Template, val playerProfile: PlayerProfile): EventCancellable<Failure>() {

        init {
            async(!Bukkit.isPrimaryThread())
        }
    }

    /**
     * 当任务重置时
     */
    class Reset(val template: Template, val playerProfile: PlayerProfile): EventCancellable<Reset>() {

        init {
            async(!Bukkit.isPrimaryThread())
        }
    }
}