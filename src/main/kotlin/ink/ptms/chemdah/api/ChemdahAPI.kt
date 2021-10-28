package ink.ptms.chemdah.api

import ink.ptms.chemdah.Chemdah
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.conversation.Conversation
import ink.ptms.chemdah.core.conversation.ConversationLoader
import ink.ptms.chemdah.core.conversation.ConversationManager
import ink.ptms.chemdah.core.conversation.Session
import ink.ptms.chemdah.core.conversation.theme.Theme
import ink.ptms.chemdah.core.database.Database
import ink.ptms.chemdah.core.quest.QuestLoader
import ink.ptms.chemdah.core.quest.Template
import ink.ptms.chemdah.core.quest.addon.Addon
import ink.ptms.chemdah.core.quest.meta.Meta
import ink.ptms.chemdah.core.quest.objective.Objective
import ink.ptms.chemdah.core.quest.objective.bukkit.EMPTY_EVENT
import ink.ptms.chemdah.core.quest.objective.other.ITrigger
import ink.ptms.chemdah.module.Module
import ink.ptms.chemdah.util.increaseAny
import org.bukkit.entity.Player
import org.bukkit.event.Event
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.warning
import taboolib.module.kether.Workspace
import taboolib.module.kether.printKetherErrorMessage
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

object ChemdahAPI {

    val workspace = Workspace(File(getDataFolder(), "module/script"), namespace = listOf("chemdah", "adyeshach"))

    val conversation = HashMap<String, Conversation>()
    val conversationTheme = HashMap<String, Theme<*>>()

    val questMeta = HashMap<String, Class<out Meta<*>>>()
    val questAddon = HashMap<String, Class<out Addon>>()
    val questTemplate = HashMap<String, Template>()
    val questObjective = HashMap<String, Objective<*>>()

    val playerProfile = ConcurrentHashMap<String, PlayerProfile>()

    /**
     * 获取已经缓存的玩家数据
     * 如玩家数据不存在则会直接抛出 NullPointerException 异常
     *
     * @throws NullPointerException
     */
    val Player.chemdahProfile: PlayerProfile
        @Throws(NullPointerException::class)
        get() = playerProfile[name]!!

    /**
     * 玩家数据已经加载完成
     */
    val Player.isChemdahProfileLoaded: Boolean
        get() = playerProfile.containsKey(name)

    /**
     * 玩家数据尚未加载完成
     */
    val Player.nonChemdahProfileLoaded: Boolean
        get() = !playerProfile.containsKey(name)

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
        chemdahProfile.getQuests(openAPI = true).forEach { quest ->
            quest.tasks.forEach { task ->
                val trigger = task.objective as? ITrigger
                if (trigger?.getValue(task) == value) {
                    QuestLoader.handleTask(chemdahProfile, task, quest, EMPTY_EVENT)
                }
            }
        }
    }

    /**
     * 获取对话资源
     */
    fun getConversation(id: String): Conversation? {
        return conversation[id]
    }

    /**
     * 注册任务模板
     */
    fun addConversation(id: String, con: Conversation) {
        conversation[id] = con
    }

    /**
     * 获取对话模式
     */
    fun getConversationTheme(id: String): Theme<*>? {
        return conversationTheme[id]
    }

    /**
     * 注册任务模板
     */
    fun addConversationTheme(id: String, theme: Theme<*>) {
        conversationTheme[id] = theme
    }

    /**
     * 获取任务模板
     */
    fun getQuestTemplate(id: String): Template? {
        return questTemplate[id]
    }

    /**
     * 注册任务模板
     */
    fun addQuestTemplate(id: String, template: Template) {
        questTemplate[id] = template
    }

    /**
     * 获取任务元数据
     */
    fun getQuestMeta(id: String): Class<out Meta<*>>? {
        return questMeta[id]
    }

    /**
     * 注册任务元数据
     */
    fun addQuestMeta(id: String, meta: Class<out Meta<*>>) {
        questMeta[id] = meta
    }

    /**
     * 获取任务扩展（组件）
     */
    fun getQuestAddon(id: String): Class<out Addon>? {
        return questAddon[id]
    }

    /**
     * 注册任务扩展（组件）
     */
    fun addQuestAddon(id: String, addon: Class<out Addon>) {
        questAddon[id] = addon
    }

    /**
     * 获取任务目标
     */
    fun getQuestObjective(id: String): Objective<*>? {
        return questObjective[id]
    }

    /**
     * 注册任务元数据
     */
    fun addQuestObjective(id: String, objective: Objective<*>) {
        questObjective[id] = objective
    }

    /**
     * 获取插件功能模块
     * 如模块不存在则会直接抛出 NullPointerException 异常
     *
     * @throws NullPointerException
     */
    @Throws(NullPointerException::class)
    fun <T : Module> getModule(kClass: KClass<T>): Module {
        return Module.modules[kClass.simpleName]!!
    }

    /**
     * 获取所有全局变量
     */
    fun getVariables(): List<String> {
        return Database.INSTANCE.variables()
    }

    /**
     * 获取全局变量
     */
    fun getVariable(key: String): String? {
        return Database.INSTANCE.selectVariable(key)
    }

    /**
     * 设置全局变量
     * @param append 是否追加模式
     */
    fun setVariable(key: String, value: String?, append: Boolean = false, default: String? = null) {
        when {
            value == null -> {
                Database.INSTANCE.releaseVariable(key)
            }
            append -> {
                Database.INSTANCE.updateVariable(key, (Database.INSTANCE.selectVariable(key) ?: default).increaseAny(value).toString())
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

    @Awake(LifeCycle.ACTIVE)
    internal fun workspace() {
        try {
            workspace.loadAll()
        } catch (e: Exception) {
            warning("[Chemdah] An error occurred while loading the script")
            e.printKetherErrorMessage()
        }
    }
}