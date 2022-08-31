package ink.ptms.chemdah.api

import ink.ptms.chemdah.api.event.plugin.CollectEvent
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Quest

/**
 * Chemdah
 * ink.ptms.chemdah.api.ChemdahEventFactory
 *
 * @author 坏黑
 * @since 2022/7/20 16:43
 */
open class ChemdahEventFactory {

    protected val questCollectCallback = arrayListOf<CollectEvent>()

    open fun prepareQuestCollect(consumer: CollectEvent) {
        questCollectCallback.add(consumer)
    }

    open fun callQuestCollect(playerProfile: PlayerProfile, quests: List<Quest>): List<Quest> {
        val list = quests.toMutableList()
        questCollectCallback.forEach { it(playerProfile, list) }
        return list
    }
}