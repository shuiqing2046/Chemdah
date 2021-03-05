package ink.ptms.chemdah.core.quest.option

import ink.ptms.chemdah.core.quest.Id
import ink.ptms.chemdah.core.quest.QuestContainer
import ink.ptms.chemdah.util.toTime
import java.text.SimpleDateFormat

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
    val real = try {
        SimpleDateFormat("yyyy/M/d H:m").parse(source).time
    } catch (ignore: Throwable) {
        0
    }

    companion object {

        fun QuestContainer.isTimeout(startTime: Long): Boolean {
            val meta = meta<MetaTimeout>("timeout") ?: return false
            return (meta.real > 0 && meta.real < System.currentTimeMillis()) || meta.timeout?.`in`(startTime)?.isTimeout(startTime) == true
        }
    }
}