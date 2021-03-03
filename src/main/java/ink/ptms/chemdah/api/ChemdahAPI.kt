package ink.ptms.chemdah.api

import ink.ptms.chemdah.Chemdah
import ink.ptms.chemdah.core.conversation.*
import ink.ptms.chemdah.core.conversation.theme.Theme
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Template
import ink.ptms.chemdah.core.quest.addon.Addon
import ink.ptms.chemdah.core.quest.meta.Meta
import ink.ptms.chemdah.core.quest.objective.Objective
import org.bukkit.entity.Player
import org.bukkit.event.Event
import java.util.concurrent.ConcurrentHashMap

object ChemdahAPI {

    val conversation = HashMap<String, Conversation>()
    val conversationTheme = HashMap<String, Theme>()

    val quest = HashMap<String, Template>()
    val questMeta = HashMap<String, Class<out Meta>>()
    val questAddon = HashMap<String, Class<out Addon>>()
    val questObjective = HashMap<String, Objective<out Event>>()

    val playerProfile = ConcurrentHashMap<String, PlayerProfile>()

    /**
     * 获取正在进行的回话
     */
    fun getConversationSession(player: Player) = ConversationManager.sessions[player.name]

    /**
     * 获取对话资源
     */
    fun getConversation(id: String) = conversation[id]

    /**
     * 获取对话模式
     */
    fun getConversationTheme(id: String) = conversationTheme[id]

    /**
     * 获取任务模板
     */
    fun getQuestTemplate(id: String) = quest[id]

    /**
     * 获取任务元数据
     */
    fun getQuestMeta(id: String) = questMeta[id]

    /**
     * 获取任务扩展
     */
    fun getQuestAddon(id: String) = questAddon[id]

    /**
     * 获取任务目标
     */
    fun getQuestObjective(id: String) = questObjective[id]

    /**
     * 获取已经缓存的玩家数据
     */
    fun getPlayerProfile(player: Player) = playerProfile[player.name]!!

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