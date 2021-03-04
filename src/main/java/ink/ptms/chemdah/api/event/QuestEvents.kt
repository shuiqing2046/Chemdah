package ink.ptms.chemdah.api.event

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.AgentType
import ink.ptms.chemdah.core.quest.QuestContainer
import ink.ptms.chemdah.core.quest.Template
import io.izzel.taboolib.module.event.EventCancellable
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
}