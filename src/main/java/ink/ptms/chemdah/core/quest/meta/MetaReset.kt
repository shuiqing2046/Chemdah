package ink.ptms.chemdah.core.quest.meta

import ink.ptms.chemdah.core.quest.Id
import ink.ptms.chemdah.core.quest.QuestContainer
import ink.ptms.chemdah.util.asList

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.meta.MetaAliases
 *
 * @author sky
 * @since 2021/3/1 11:47 下午
 */
@Id("reset")
@MetaType(MetaType.Type.ANY)
class MetaReset(source: Any?, questContainer: QuestContainer) : Meta<Any?>(source, questContainer) {

    val reset = source?.asList() ?: emptyList()

    companion object {

        fun QuestContainer.reset() = meta<MetaReset>("reset")?.reset ?: emptyList()
    }
}