package ink.ptms.chemdah.core.quest

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.core.DataContainer

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.Quest
 *
 * @author sky
 * @since 2021/3/2 12:03 上午
 */
class Quest(val id: String) {

    val isValid: Boolean
        get() = ChemdahAPI.getQuestTemplate(id) != null

    val template: Template
        get() = ChemdahAPI.getQuestTemplate(id)!!

    val persistentDataContainer = DataContainer()
}