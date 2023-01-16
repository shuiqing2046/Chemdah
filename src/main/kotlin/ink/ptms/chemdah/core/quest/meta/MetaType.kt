package ink.ptms.chemdah.core.quest.meta

import ink.ptms.chemdah.core.quest.Id
import ink.ptms.chemdah.core.quest.Option
import ink.ptms.chemdah.core.quest.QuestContainer
import ink.ptms.chemdah.core.quest.Template
import taboolib.common.util.asList

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.meta.MetaAliases
 *
 * @author sky
 * @since 2021/3/1 11:47 下午
 */
@Id("type")
@Option(Option.Type.ANY)
class MetaType(source: Any?, questContainer: QuestContainer) : Meta<Any?>(source, questContainer) {

    val type = source?.asList()?.flatMap { it.split("[,;]".toRegex()) }?.map { it.trim() } ?: emptyList()

    companion object {

        /** 获取任务类型 Meta */
        fun Template.type() = meta<MetaType>("type")?.type ?: emptyList()
    }
}