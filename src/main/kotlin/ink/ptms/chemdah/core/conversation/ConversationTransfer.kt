package ink.ptms.chemdah.core.conversation

import taboolib.library.configuration.ConfigurationSection

/**
 * Chemdah
 * ink.ptms.chemdah.core.conversation.ConversationTransfer
 *
 * @author 坏黑
 * @since 2021/12/11 3:32 AM
 */
open class ConversationTransfer(val root: ConfigurationSection) {

    /**
     * 转移到目标
     */
    val id = root.getString("id")
}