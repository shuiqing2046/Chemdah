package ink.ptms.chemdah.api.event.collect

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.AgentType
import ink.ptms.chemdah.core.quest.Quest
import ink.ptms.chemdah.core.quest.QuestContainer
import ink.ptms.chemdah.core.quest.Template
import taboolib.platform.type.BukkitProxyEvent

/**
 * Chemdah
 * ink.ptms.chemdah.api.event.collect.QuestEvents
 *
 * @author sky
 * @since 2021/2/21 1:07 上午
 */
class QuestEvents {

    /**
     * 当任务中当脚本代理执行时
     */
    class Agent(val questContainer: QuestContainer, val playerProfile: PlayerProfile, val agentType: AgentType, val restrict: String): BukkitProxyEvent()

    /**
     * 当任务接受时
     */
    class Accept {

        class Pre(val quest: Template, val playerProfile: PlayerProfile, var reason: String? = null): BukkitProxyEvent()

        class Post(val quest: Quest, val playerProfile: PlayerProfile): BukkitProxyEvent() {

            override val allowCancelled: Boolean
                get() = false
        }
    }

    /**
     * 当任务失败（放弃）时
     */
    class Fail {

        class Pre(val quest: Quest, val playerProfile: PlayerProfile): BukkitProxyEvent()

        class Post(val quest: Quest, val playerProfile: PlayerProfile): BukkitProxyEvent() {

            override val allowCancelled: Boolean
                get() = false
        }
    }

    /**
     * 当任务重置时
     */
    class Restart {

        class Pre(val quest: Quest, val playerProfile: PlayerProfile): BukkitProxyEvent()

        class Post(val quest: Quest, val playerProfile: PlayerProfile): BukkitProxyEvent() {

            override val allowCancelled: Boolean
                get() = false
        }
    }

    /**
     * 当任务完成时
     */
    class Complete {

        class Pre(val quest: Quest, val playerProfile: PlayerProfile): BukkitProxyEvent()

        class Post(val quest: Quest, val playerProfile: PlayerProfile): BukkitProxyEvent() {

            override val allowCancelled: Boolean
                get() = false
        }
    }

    /**
     * 当任务被注册到玩家数据
     */
    class Registered(val quest: Quest, val playerProfile: PlayerProfile): BukkitProxyEvent() {

        override val allowCancelled: Boolean
            get() = false
    }

    /**
     * 当任务从玩家数据中注销
     */
    class Unregistered(val quest: Quest, val playerProfile: PlayerProfile): BukkitProxyEvent() {

        override val allowCancelled: Boolean
            get() = false
    }

    @Deprecated("请使用 ChemdahAPI.eventFactory")
    class Collect(val quests: MutableList<Quest>, val playerProfile: PlayerProfile): BukkitProxyEvent()
}