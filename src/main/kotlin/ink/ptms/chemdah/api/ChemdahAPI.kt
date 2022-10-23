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
import ink.ptms.chemdah.core.quest.TemplateGroup
import ink.ptms.chemdah.core.quest.addon.Addon
import ink.ptms.chemdah.core.quest.meta.Meta
import ink.ptms.chemdah.core.quest.objective.Objective
import ink.ptms.chemdah.core.quest.objective.bukkit.EMPTY_EVENT
import ink.ptms.chemdah.core.quest.objective.other.ITrigger
import ink.ptms.chemdah.module.Module
import ink.ptms.chemdah.util.increaseAny
import ink.ptms.chemdah.util.namespace
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.warning
import taboolib.module.kether.KetherFunction
import taboolib.module.kether.KetherShell
import taboolib.module.kether.Workspace
import taboolib.module.kether.printKetherErrorMessage
import taboolib.module.lang.Language
import java.io.File
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

object ChemdahAPI {

    /** 脚本工作空间 **/
    val workspace = Workspace(File(getDataFolder(), "module/script"), namespace = namespace)

    /** 已注册对话 **/
    val conversation = HashMap<String, Conversation>()
    /** 已注册主题 **/
    val conversationTheme = HashMap<String, Theme<*>>()

    /** 已注册任务元数据 **/
    val questMeta = HashMap<String, Class<out Meta<*>>>()
    /** 已注册任务组件 **/
    val questAddon = HashMap<String, Class<out Addon>>()
    /** 已注册任务模板 **/
    val questTemplate = HashMap<String, Template>()
    /** 已注册任务模板分组 **/
    val questTemplateGroup = HashMap<String, TemplateGroup>()
    /** 已注册任务目标 **/
    val questObjective = HashMap<String, Objective<*>>()

    /** 玩家数据 **/
    val playerProfile = ConcurrentHashMap<String, PlayerProfile>()

    /** 事件工厂 **/
    var eventFactory = ChemdahEventFactory()

    /**
     * 获取已经缓存的玩家数据
     * 如玩家数据不存在则会直接抛出 NullPointerException 异常
     *
     * @throws NullPointerException
     */
    val Player.chemdahProfile: PlayerProfile
        @Throws(NullPointerException::class)
        get() = ChemdahAPI.playerProfile[name]!!

    /**
     * 玩家数据已经加载完成
     */
    val Player.isChemdahProfileLoaded: Boolean
        get() = ChemdahAPI.playerProfile.containsKey(name)

    /**
     * 玩家数据尚未加载完成
     */
    val Player.nonChemdahProfileLoaded: Boolean
        get() = !ChemdahAPI.playerProfile.containsKey(name)

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
                if (trigger?.getValues(task)?.contains(value) == true) {
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
     * 获取任务模板组
     */
    fun getQuestTemplateGroup(id: String): TemplateGroup? {
        return questTemplateGroup[id]
    }

    /**
     * 注册任务模板组
     */
    fun addQuestTemplateGroup(id: String, templateGroup: TemplateGroup) {
        questTemplateGroup[id] = templateGroup
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
        Language.reload()
        Chemdah.conf.reload()
        ConversationManager.conf.reload()
        ConversationLoader.loadAll()
        QuestLoader.loadTemplate()
        QuestLoader.loadTemplateGroup()
        Module.reload()
    }

    fun invokeKether(source: String, player: Player? = null, vars: Map<String, Any> = emptyMap()): CompletableFuture<Any?> {
        val map = KetherShell.VariableMap(*vars.map { it.key to it.value }.toTypedArray())
        return KetherShell.eval(source, sender = if (player != null) adaptPlayer(player) else null, namespace = namespace, vars = map)
    }

    fun parseFunction(source: String, player: Player? = null, vars: Map<String, Any> = emptyMap()): String {
        val map = KetherShell.VariableMap(*vars.map { it.key to it.value }.toTypedArray())
        return KetherFunction.parse(source, sender = if (player != null) adaptPlayer(player) else null, namespace = namespace, vars = map)
    }

    @Awake(LifeCycle.ACTIVE)
    internal fun workspace() {
        try {
            workspace.loadAll()
        } catch (e: Exception) {
            warning("An error occurred while loading the script")
            e.printKetherErrorMessage()
        }
    }
}