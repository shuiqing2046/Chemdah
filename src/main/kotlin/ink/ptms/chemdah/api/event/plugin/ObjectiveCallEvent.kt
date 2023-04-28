package ink.ptms.chemdah.api.event.plugin

import ink.ptms.chemdah.core.quest.objective.Objective

/**
 * Chemdah
 * ink.ptms.chemdah.api.event.plugin.ObjectiveCallEvent
 *
 * @author 坏黑
 * @since 2022/7/20 16:47
 */
interface ObjectiveCallEvent {

    operator fun invoke(objective: Objective<*>, event: Any)
}