package ink.ptms.chemdah.core.quest.meta

import ink.ptms.chemdah.core.quest.Id
import ink.ptms.chemdah.core.quest.QuestContainer
import ink.ptms.chemdah.core.quest.Template
import ink.ptms.chemdah.util.asList

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.meta.MetaAliases
 *
 * @author sky
 * @since 2021/3/1 11:47 下午
 */
@Id("label")
@MetaType(MetaType.Type.ANY)
class MetaLabel(source: Any?, questContainer: QuestContainer) : Meta<Any?>(source, questContainer) {

    val label = source?.asList() ?: emptyList()

    companion object {

        fun Template.label() = meta<MetaLabel>("label")?.label ?: emptyList()
    }
}