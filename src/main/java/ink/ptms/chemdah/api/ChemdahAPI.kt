package ink.ptms.chemdah.api

import ink.ptms.chemdah.Chemdah
import ink.ptms.chemdah.core.conversation.*
import ink.ptms.chemdah.core.conversation.theme.Theme
import ink.ptms.chemdah.core.quest.Template
import ink.ptms.chemdah.core.quest.addon.Addon
import ink.ptms.chemdah.core.quest.meta.Meta
import ink.ptms.chemdah.core.quest.objective.Objective
import org.bukkit.entity.Player

object ChemdahAPI {

    val conversation = HashMap<String, Conversation>()
    val conversationTheme = HashMap<String, Theme>()

    val quest = HashMap<String, Template>()
    val questMeta = HashMap<String, Class<out Meta>>()
    val questAddon = HashMap<String, Class<out Addon>>()
    val questObjective = HashMap<String, Objective>()

    fun getConversationSession(player: Player) = ConversationManager.sessions[player.name]

    fun getConversation(id: String) = conversation[id]

    fun getConversationTheme(id: String) = conversationTheme[id]

    fun getQuestTemplate(id: String) = quest[id]

    fun getQuestMeta(id: String) = questMeta[id]

    fun getQuestAddon(id: String) = questAddon[id]

    fun getQuestObjective(id: String) = questObjective[id]

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
    }
}