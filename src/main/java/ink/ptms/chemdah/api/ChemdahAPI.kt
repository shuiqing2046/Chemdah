package ink.ptms.chemdah.api

import ink.ptms.chemdah.Chemdah
import ink.ptms.chemdah.core.conversation.*
import org.bukkit.entity.Player

object ChemdahAPI {

    val conversation = HashMap<String, Conversation>()
    val conversationTheme = HashMap<String, Theme>()

    /**
     * 获取对话
     *
     * @param id 名称
     */
    fun getConversation(id: String): Conversation? {
        return conversation[id]
    }

    /**
     * 获取对话展示模式
     *
     * @param id 名称
     */
    fun getConversationTheme(id: String): Theme? {
        return conversationTheme[id]
    }

    /**
     * 获取会话实例
     *
     * @param player 玩家
     */
    fun getConversationSession(player: Player): Session? {
        return ConversationManager.sessions[player.name]
    }

    /**
     * 1。重载中心配置文件
     * 2。重载对话配置文件
     * 3。重载对话
     * 4。重载对话展示模式
     */
    fun reloadAll() {
        Chemdah.conf.reload()
        ConversationManager.conf.reload()
        ConversationLoader.load()
        conversationTheme.values.forEach { it.reloadConfig() }
    }
}