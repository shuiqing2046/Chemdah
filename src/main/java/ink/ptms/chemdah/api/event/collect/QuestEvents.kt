package ink.ptms.chemdah.api.event.collect

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.AgentType
import ink.ptms.chemdah.core.quest.Quest
import ink.ptms.chemdah.core.quest.QuestContainer
import ink.ptms.chemdah.core.quest.Template
import io.izzel.taboolib.module.event.EventCancellable
import io.izzel.taboolib.module.event.EventNormal

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
    class Agent(val questContainer: QuestContainer, val playerProfile: PlayerProfile, val agentType: AgentType, val restrict: String): EventCancellable<Agent>(true)

    /**
     * 当玩家的任务列表被获取时
     * 通过修改这个属性来动态编辑玩家的任务列表
     */
    class Collect(val quests: MutableList<Quest>, val playerProfile: PlayerProfile) : EventNormal<Collect>(true)

    /**
     * 当任务接受时
     */
    class Accept {

        class Pre(val quest: Template, val playerProfile: PlayerProfile, var reason: String? = null): EventCancellable<Pre>(true)

        class Post(val quest: Quest, val playerProfile: PlayerProfile): EventNormal<Post>(true)
    }

    /**
     * 当任务失败（放弃）时
     */
    class Fail {

        class Pre(val quest: Quest, val playerProfile: PlayerProfile): EventCancellable<Pre>(true)

        class Post(val quest: Quest, val playerProfile: PlayerProfile): EventNormal<Post>(true)
    }

    /**
     * 当任务重置时
     */
    class Restart {

        class Pre(val quest: Quest, val playerProfile: PlayerProfile): EventCancellable<Pre>(true)

        class Post(val quest: Quest, val playerProfile: PlayerProfile): EventNormal<Post>(true)
    }

    /**
     * 当任务完成时
     */
    class Complete {

        class Pre(val quest: Quest, val playerProfile: PlayerProfile): EventCancellable<Pre>(true)

        class Post(val quest: Quest, val playerProfile: PlayerProfile): EventNormal<Post>(true)
    }

    /**
     * 当任务被注册到玩家数据
     */
    class Registered(val quest: Quest, val playerProfile: PlayerProfile): EventNormal<Registered>(true)

    /**
     * 当任务从玩家数据中注销
     */
    class Unregistered(val quest: Quest, val playerProfile: PlayerProfile): EventNormal<Unregistered>(true)
}