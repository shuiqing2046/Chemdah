package ink.ptms.chemdah.api

import ink.ptms.chemdah.Chemdah
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.conversation.Conversation
import ink.ptms.chemdah.core.conversation.ConversationLoader
import ink.ptms.chemdah.core.conversation.ConversationManager
import ink.ptms.chemdah.core.conversation.Session
import ink.ptms.chemdah.core.conversation.theme.Theme
import ink.ptms.chemdah.core.database.Database
import ink.ptms.chemdah.core.quest.Idx
import ink.ptms.chemdah.core.quest.QuestLoader
import ink.ptms.chemdah.core.quest.Template
import ink.ptms.chemdah.core.quest.addon.Addon
import ink.ptms.chemdah.core.quest.meta.Meta
import ink.ptms.chemdah.core.quest.meta.MetaAlias.Companion.alias
import ink.ptms.chemdah.core.quest.meta.MetaLabel.Companion.label
import ink.ptms.chemdah.core.quest.objective.Objective
import ink.ptms.chemdah.core.quest.objective.bukkit.EMPTY_EVENT
import ink.ptms.chemdah.core.quest.objective.other.ITrigger
import ink.ptms.chemdah.module.Module
import ink.ptms.chemdah.util.increaseAny
import io.izzel.taboolib.kotlin.Mirror
import org.bukkit.entity.Player
import org.bukkit.event.Event
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

object ChemdahAPI {

    val mirror = Mirror()

    val conversation = HashMap<String, Conversation>()
    val conversationTheme = HashMap<String, Theme<*>>()

    val quest = HashMap<String, Template>()
    val questMeta = HashMap<String, Class<out Meta<*>>>()
    val questAddon = HashMap<String, Class<out Addon>>()
    val questObjective = HashMap<String, Objective<out Event>>()

    val playerProfile = ConcurrentHashMap<String, PlayerProfile>()

    /**
     * 获取已经缓存的玩家数据
     * 如玩家不存在则会直接抛出 NullPointerException 异常
     *
     * @throws NullPointerException
     */
    val Player.chemdahProfile: PlayerProfile
        @Throws(NullPointerException::class)
        get() = ChemdahAPI.playerProfile[name]!!

    /**
     * 玩家数据是否加载完成
     */
    val Player.isChemdahProfileLoaded: Boolean
        get() = ChemdahAPI.playerProfile.containsKey(name)

    /**
     * 获取玩家正在进行的会话
     */
    val Player.conversationSession: Session?
       get() = ConversationManager.sessions[name]

    /**
     * 唤起玩家的 Trigger 类型任务
     */
    fun Player.callTrigger(value: String) {
        val chemdahProfile = chemdahProfile
        chemdahProfile.quests.forEach { quest ->
            quest.tasks.forEach { task ->
                val trigger = task.objective as? ITrigger
                if (trigger?.getValue(task) == value) {
                    QuestLoader.handleTask(chemdahProfile, task.objective, task, EMPTY_EVENT)
                }
            }
        }
    }

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
     * 通过序号、别名或标签获取所有符合要求的任务模板
     */
    fun getQuestTemplate(value: String, idx: Idx = Idx.ID): List<Template> {
        return when (idx) {
            Idx.ID -> {
                quest.filterValues { it.id == value }.values.toList()
            }
            Idx.ID_ALIAS -> {
                quest.filterValues { it.id == value || it.alias() == value }.values.toList()
            }
            Idx.LABEL -> {
                quest.filterValues { value in it.label() }.values.toList()
            }
        }
    }

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
     * 获取已注册的模块
     * 如模块不存在则会直接抛出 NullPointerException 异常
     *
     * @throws NullPointerException
     */
    @Throws(NullPointerException::class)
    fun <T : Module> getModule(kClass: KClass<T>) = Module.modules[kClass.simpleName]!!

    /**
     * 获取所有全局变量
     */
    fun getVariables() = Database.INSTANCE.variables()

    /**
     * 获取全局变量
     */
    fun getVariable(key: String) = Database.INSTANCE.selectVariable(key)

    /**
     * 设置全局变量
     */
    fun setVariable(key: String, value: String?, append: Boolean = false) {
        when {
            value == null -> {
                Database.INSTANCE.releaseVariable(key)
            }
            append -> {
                Database.INSTANCE.updateVariable(key, Database.INSTANCE.selectVariable(key).increaseAny(value).toString())
            }
            else -> {
                Database.INSTANCE.updateVariable(key, value)
            }
        }
    }

    /**
     * 1 重载中心配置文件
     * 2 重载对话配置文件
     * 3 重载对话
     * 4 重载对话展示模式
     * 5 重载任务
     * 6 重载模块
     */
    fun reloadAll() {
        Chemdah.conf.reload()
        ConversationManager.conf.reload()
        ConversationLoader.load()
        QuestLoader.loadTemplate()
        Module.reload()
    }
}