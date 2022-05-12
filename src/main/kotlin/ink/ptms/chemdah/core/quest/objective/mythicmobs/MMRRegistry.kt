package ink.ptms.chemdah.core.quest.objective.mythicmobs

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.api.Mythic
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.mythicmobs.MMRRegistry
 *
 * @author 坏黑
 * @since 2022/5/12 09:11
 */
object MMRRegistry {

    @Awake(LifeCycle.ENABLE)
    fun init() {
        if (Mythic.isLegacy) {
            ChemdahAPI.addQuestObjective("mythicmobs kill", MMythicKillType4)
        } else {
            ChemdahAPI.addQuestObjective("mythicmobs kill", MMythicKillType5)
        }
    }
}