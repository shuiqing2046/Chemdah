package ink.ptms.chemdah.core.quest.meta

import ink.ptms.chemdah.core.quest.Id
import ink.ptms.chemdah.core.quest.QuestContainer
import ink.ptms.chemdah.core.quest.Template

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.meta.MetaAliases
 *
 * @author sky
 * @since 2021/3/1 11:47 下午
 */
@Id("alias")
@MetaType(MetaType.Type.TEXT)
class MetaAlias(source: String?, questContainer: QuestContainer) : Meta<String?>(source, questContainer) {

    val alias = source

    companion object {

        fun Template.alias() = meta<MetaAlias>("alias")?.alias
    }
}