package ink.ptms.chemdah.api.event.collect

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Quest
import ink.ptms.chemdah.core.quest.Task
import ink.ptms.chemdah.core.quest.objective.Objective
import taboolib.platform.type.BukkitProxyEvent

/**
 * Chemdah
 * ink.ptms.chemdah.api.event.collect.ObjectiveEvents
 *
 * @author sky
 * @since 2021/2/21 1:07 上午
 */
class ObjectiveEvents {

    /**
     * 当条目继续时
     */
    class Continue {

        class Pre(val objective: Objective<*>, val task: Task, val quest: Quest, val playerProfile: PlayerProfile): BukkitProxyEvent()

        class Post(val objective: Objective<*>, val task: Task, val quest: Quest, val playerProfile: PlayerProfile): BukkitProxyEvent() {

            override val allowCancelled: Boolean
                get() = false
        }
    }

    /**
     * 当条目完成时
     */
    class Complete {

        class Pre(val objective: Objective<*>, val task: Task, val quest: Quest, val playerProfile: PlayerProfile): BukkitProxyEvent()

        class Post(val objective: Objective<*>, val task: Task, val quest: Quest, val playerProfile: PlayerProfile): BukkitProxyEvent() {

            override val allowCancelled: Boolean
                get() = false
        }
    }

    /**
     * 当条目重置时
     */
    class Restart {

        class Pre(val objective: Objective<*>, val task: Task, val quest: Quest, val playerProfile: PlayerProfile): BukkitProxyEvent()

        class Post(val objective: Objective<*>, val task: Task, val quest: Quest, val playerProfile: PlayerProfile): BukkitProxyEvent() {

            override val allowCancelled: Boolean
                get() = false
        }
    }
}