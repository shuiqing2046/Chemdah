package ink.ptms.chemdah.api

import ink.ptms.chemdah.api.event.plugin.CollectEvent
import ink.ptms.chemdah.api.event.plugin.ObjectiveCallEvent
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Quest
import ink.ptms.chemdah.core.quest.objective.Objective

/**
 * Chemdah
 * ink.ptms.chemdah.api.ChemdahEventFactory
 *
 * @author 坏黑
 * @since 2022/7/20 16:43
 */
open class ChemdahEventFactory {

    protected val questCollectCallback = arrayListOf<CollectEvent>()
    protected val objectiveCallCallback = arrayListOf<ObjectiveCallEvent>()

    open fun prepareObjectiveCall(consumer: ObjectiveCallEvent) {
        objectiveCallCallback.add(consumer)
    }

    open fun prepareQuestCollect(consumer: CollectEvent) {
        questCollectCallback.add(consumer)
    }

    open fun callQuestCollect(playerProfile: PlayerProfile, quests: List<Quest>): List<Quest> {
        val list = quests.toMutableList()
        questCollectCallback.forEach { it(playerProfile, list) }
        return list
    }

    open fun callObjectiveCall(objective: Objective<*>, event: Any): Boolean {
        objectiveCallCallback.forEach { it(objective, event) }
        return true
    }
}