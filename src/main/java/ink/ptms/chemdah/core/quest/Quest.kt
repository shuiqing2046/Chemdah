package ink.ptms.chemdah.core.quest

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.core.Metadata
import ink.ptms.chemdah.core.quest.meta.Meta
import org.bukkit.configuration.ConfigurationSection

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.Quest
 *
 * @author sky
 * @since 2021/3/2 12:03 上午
 */
class Quest(val id: String) {

    val template: Template?
        get() = ChemdahAPI.getQuestTemplate(id)

    val metadata = Metadata()
}