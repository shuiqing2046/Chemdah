package ink.ptms.chemdah.core.quest.addon

import ink.ptms.chemdah.core.quest.Id
import ink.ptms.chemdah.core.quest.Option
import ink.ptms.chemdah.core.quest.QuestContainer
import taboolib.common5.util.parseTimeCycle
import java.text.SimpleDateFormat

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.addon.AddonTimeout
 *
 * @author sky
 * @since 2021/3/4 9:04 上午
 */
@Id("timeout")
@Option(Option.Type.TEXT)
class AddonTimeout(root: String, questContainer: QuestContainer) : Addon(root, questContainer) {

    val timeout = root.parseTimeCycle()
    val real = try {
        SimpleDateFormat("yyyy/M/d H:m").parse(root).time
    } catch (ignore: Throwable) {
        0
    }

    companion object {

        fun QuestContainer.isTimeout(startTime: Long): Boolean {
            val meta = addon<AddonTimeout>("timeout") ?: return false
            return (meta.real > 0 && meta.real < System.currentTimeMillis()) || meta.timeout.start(startTime)?.isTimeout(startTime) == true
        }
    }
}