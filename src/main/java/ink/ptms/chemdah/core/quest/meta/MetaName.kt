package ink.ptms.chemdah.core.quest.meta

import ink.ptms.chemdah.core.quest.Id
import ink.ptms.chemdah.core.quest.QuestContainer

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.meta.MetaName
 *
 * @author sky
 * @since 2021/3/1 11:47 下午
 */
@Id("name")
@MetaType(MetaType.Type.TEXT)
class MetaName(source: String?, questContainer: QuestContainer) : Meta<String?>(source, questContainer) {

    val displayName = source.toString()

    companion object {

        fun QuestContainer.displayName() = meta<MetaName>("name")?.displayName ?: id
    }
}