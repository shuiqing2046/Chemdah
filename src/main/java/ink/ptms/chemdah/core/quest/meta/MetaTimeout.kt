package ink.ptms.chemdah.core.quest.meta

import ink.ptms.chemdah.core.quest.Id
import ink.ptms.chemdah.core.quest.QuestContainer
import ink.ptms.chemdah.core.quest.Task
import ink.ptms.chemdah.core.quest.Template
import ink.ptms.chemdah.util.toTime

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.meta.MetaTimeout
 *
 * @author sky
 * @since 2021/3/4 9:04 上午
 */
@Id("timeout")
@MetaType(MetaType.Type.TEXT)
class MetaTimeout(source: String?, questContainer: QuestContainer) : Meta<String?>(source, questContainer) {

    val timeout = source?.toTime()

    companion object {

        fun Task.timeout() = meta<MetaTimeout>("timeout")?.timeout

        fun Template.timeout() = meta<MetaTimeout>("timeout")?.timeout
    }
}