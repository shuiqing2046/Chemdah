package ink.ptms.chemdah.core.quest

import ink.ptms.chemdah.Chemdah
import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.core.quest.addon.Addon
import ink.ptms.chemdah.core.quest.objective.Objective
import ink.ptms.chemdah.core.quest.meta.Meta
import ink.ptms.chemdah.util.SingleListener
import ink.ptms.chemdah.util.mirrorFuture
import io.izzel.taboolib.TabooLibLoader
import io.izzel.taboolib.compat.kotlin.CompatKotlin
import io.izzel.taboolib.kotlin.Tasks
import io.izzel.taboolib.module.db.local.SecuredFile
import io.izzel.taboolib.module.inject.TFunction
import io.izzel.taboolib.module.inject.TSchedule
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Event
import java.io.File

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.QuestManager
 *
 * @author sky
 * @since 2021/3/2 1:13 上午
 */
object QuestHandler {

    @TSchedule(period = 20, async = true)
    private fun tick() {
        mirrorFuture("QuestHandler:tick") {
            Bukkit.getOnlinePlayers().forEach {
                ChemdahAPI.getPlayerProfile(it).also { profile ->
                    // 检测所有有效任务
                    profile.quests.forEach { quest ->
                        // 检测超时
                        if (quest.isTimeout) {
                            quest.failureQuest()
                        } else {
                            // 检查条目自动完成
                            quest.tasks.forEach { task ->
                                task.objective.checkComplete(profile, task)
                            }
                            // 检查任务自动完成
                            quest.checkComplete()
                        }
                    }
                }
            }
            finish()
        }
    }

    @Suppress("UNCHECKED_CAST")
    @TFunction.Init
    private fun register() {
        TabooLibLoader.getPluginClassSafely(Chemdah.plugin).forEach {
            if (Objective::class.java.isAssignableFrom(it)) {
                val objective = CompatKotlin.getInstance(it) as? Objective<Event>
                if (objective != null) {
                    ChemdahAPI.questObjective[objective.name] = objective
                    // 是否注册监听器
                    if (objective.isListener) {
                        // 对该条目注册独立监听器
                        SingleListener.listen(objective.event.java, objective.priority, objective.ignoreCancelled) { e ->
                            // 获取该监听器中的玩家对象
                            objective.handler(e)?.run {
                                if (objective.isAsync) {
                                    Tasks.task(true) {
                                        handleEvent(this, e, objective)
                                    }
                                } else {
                                    handleEvent(this, e, objective)
                                }
                            }
                        }
                    }
                }
            } else if (it.isAnnotationPresent(Id::class.java)) {
                val id = it.getAnnotation(Id::class.java).id
                when {
                    Meta::class.java.isAssignableFrom(it) -> {
                        ChemdahAPI.questMeta[id] = it as Class<out Meta<*>>
                    }
                    Addon::class.java.isAssignableFrom(it) -> {
                        ChemdahAPI.questAddon[id] = it as Class<out Addon>
                    }
                }
            }
        }
        loadTemplate()
    }

    /**
     * 处理事件所对应的条目类型
     * 会进行完成检测
     *
     * @param player 玩家
     * @param event 事件
     * @param objective 条目类型
     */
    fun handleEvent(player: Player, event: Event, objective: Objective<Event>) {
        mirrorFuture("QuestHandler:handleEvent:${objective.name}") {
            ChemdahAPI.getPlayerProfile(player).also { profile ->
                // 通过事件获取所有正在进行的任务条目
                profile.getTasks(event).forEach { task ->
                    // 如果含有完成标记，则不在进行该条目
                    if (objective.hasCompletedSignature(profile, task)) {
                        return@forEach
                    }
                    // 判断条件并进行该条目
                    objective.checkCondition(profile, task, event).thenAccept { cond ->
                        if (cond) {
                            objective.onContinue(profile, task, event)
                            objective.checkComplete(profile, task)
                            task.getQuest(profile)?.checkComplete()
                        }
                    }
                }
            }
            finish()
        }
    }

    fun loadTemplate() {
        val file = File(Chemdah.plugin.dataFolder, "quest")
        if (!file.exists()) {
            Chemdah.plugin.saveResource("quest/example.yml", true)
        }
        ChemdahAPI.quest.clear()
        ChemdahAPI.quest.putAll(loadTemplate(file).map { it.id to it })
        println("[Chemdah] ${ChemdahAPI.quest.size} template loaded.")
    }

    fun loadTemplate(file: File): List<Template> {
        return when {
            file.isDirectory -> {
                file.listFiles()?.flatMap { loadTemplate(it) }?.toList() ?: emptyList()
            }
            file.name.endsWith(".yml") -> {
                SecuredFile.loadConfiguration(file).run {
                    getKeys(false).map { Template(it, getConfigurationSection(it)!!) }
                }
            }
            else -> {
                emptyList()
            }
        }
    }
}