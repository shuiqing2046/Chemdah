package ink.ptms.chemdah.core.quest.meta

import ink.ptms.chemdah.core.quest.Id
import ink.ptms.chemdah.core.quest.Option
import ink.ptms.chemdah.core.quest.QuestContainer
import ink.ptms.chemdah.core.quest.Task
import taboolib.module.chat.colored

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.meta.MetaName
 *
 * @author sky
 * @since 2021/3/1 11:47 下午
 */
@Id("name")
@Option(Option.Type.TEXT)
class MetaName(source: String?, questContainer: QuestContainer) : Meta<String?>(source, questContainer) {

    val displayName = source?.colored()

    companion object {

        fun QuestContainer.displayName(): String {
            return meta<MetaName>("name")?.displayName ?: if (this is Task) template.displayName() else id
        }
    }
}