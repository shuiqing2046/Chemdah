package ink.ptms.chemdah.core.conversation

import taboolib.library.configuration.ConfigurationSection
import taboolib.module.chat.colored

/**
 * Chemdah
 * ink.ptms.chemdah.core.conversation.LineFormat
 *
 * @author 坏黑
 * @since 2022/11/19 22:49
 */
class LineFormat(val root: ConfigurationSection) {

    /** 单行格式 **/
    val single = root.getString("single")?.colored()

    /** 顶行格式 **/
    val top = root.getString("top")?.colored()

    /** 间行格式 **/
    val body = root.getString("body")?.colored()

    /** 底行格式 **/
    val bottom = root.getString("bottom")?.colored()

    /**
     * 对消息进行格式化
     */
    fun format(message: String, idx: Int, size: Int): String {
        if (single == null || top == null || body == null || bottom == null) {
            return message
        }
        return when {
            size == 1 -> single
            idx == 0 -> top
            idx + 1 < size -> body
            else -> bottom
        }.replace("{text}", message)
    }
}