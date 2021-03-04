package ink.ptms.chemdah.core.quest

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.core.DataContainer
import ink.ptms.chemdah.core.PlayerProfile

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.Quest
 *
 * @author sky
 * @since 2021/3/2 12:03 上午
 */
class Quest(val id: String, val profile: PlayerProfile) {

    val template: Template
        get() = ChemdahAPI.getQuestTemplate(id)!!

    val isValid: Boolean
        get() = ChemdahAPI.getQuestTemplate(id) != null

    val isCompleted: Boolean
        get() = isValid && template.task.values.all { it.objective.hasCompletedSignature(profile, it) }

    val persistentDataContainer = DataContainer()
}