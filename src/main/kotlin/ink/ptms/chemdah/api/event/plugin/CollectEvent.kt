package ink.ptms.chemdah.api.event.plugin

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Quest

/**
 * Chemdah
 * ink.ptms.chemdah.api.event.plugin.CollectEvent
 *
 * @author 坏黑
 * @since 2022/7/20 16:47
 */
interface CollectEvent {

    operator fun invoke(playerProfile: PlayerProfile, quests: MutableList<Quest>)
}