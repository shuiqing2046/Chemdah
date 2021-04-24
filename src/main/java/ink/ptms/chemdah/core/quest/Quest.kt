package ink.ptms.chemdah.core.quest

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.api.event.collect.ObjectiveEvents
import ink.ptms.chemdah.api.event.collect.QuestEvents
import ink.ptms.chemdah.core.DataContainer
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.meta.MetaControl
import ink.ptms.chemdah.core.quest.meta.MetaControl.Companion.control
import ink.ptms.chemdah.core.quest.meta.MetaRestart.Companion.restart
import ink.ptms.chemdah.core.quest.meta.MetaTimeout.Companion.isTimeout
import ink.ptms.chemdah.util.mirrorFuture
import org.bukkit.entity.Player

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.Quest
 *
 * @author sky
 * @since 2021/3/2 12:03 上午
 */
class Quest(val id: String, val profile: PlayerProfile, val persistentDataContainer: DataContainer = DataContainer()) {

    val template: Template
        get() = ChemdahAPI.getQuestTemplate(id)!!

    val isValid: Boolean
        get() = ChemdahAPI.getQuestTemplate(id) != null

    val isCompleted: Boolean
        get() = isValid && template.task.all { it.value.objective.hasCompletedSignature(profile, it.value) }

    val tasks: Collection<Task>
        get() = template.task.values

    val startTime: Long
        get() = persistentDataContainer["start", 0L].toLong()

    val isTimeout: Boolean
        get() = template.isTimeout(startTime)

    /**
     * 是否为新的任务，擅自修改这个属性会导致数据出错
     */
    var newQuest = false

    init {
        persistentDataContainer.put("start", System.currentTimeMillis())
        profile.persistentDataContainer.remove("quest.complete.$id")
    }

    /**
     * 判断该玩家是否为该任务的拥有者
     */
    fun isOwner(player: Player) = profile.uniqueId == player.uniqueId

    /**
     * 获取条目
     */
    fun getTask(id: String) = tasks.firstOrNull { it.id == id }

    /**
     * 检查任务的所有条目
     * 当所有条目均已完成时任务完成
     */
    fun checkComplete() {
        mirrorFuture("Quest:checkComplete") {
            template.restart(profile).thenAccept { reset ->
                if (reset) {
                    resetQuest()
                    finish()
                } else {
                    if (tasks.all { it.objective.hasCompletedSignature(profile, it) } && QuestEvents.Complete(this@Quest, profile).call().nonCancelled()) {
                        template.agent(profile, AgentType.QUEST_COMPLETE, this@Quest).thenAccept {
                            if (it) {
                                template.control().signature(profile, MetaControl.Trigger.COMPLETE)
                                profile.unregisterQuest(this@Quest)
                                profile.persistentDataContainer.put("quest.complete.$id", System.currentTimeMillis())
                            }
                            finish()
                        }
                    } else {
                        finish()
                    }
                }
            }
        }
    }

    /**
     * 完成任务
     */
    fun completeQuest() {
        tasks.forEach { it.objective.setCompletedSignature(profile, it, true) }
        checkComplete()
    }

    /**
     * 放弃任务
     */
    fun failureQuest() {
        mirrorFuture("Quest:failure") {
            if (QuestEvents.Failure(this@Quest, profile).call().nonCancelled()) {
                template.agent(profile, AgentType.QUEST_FAILURE, this@Quest).thenAccept {
                    if (it) {
                        template.control().signature(profile, MetaControl.Trigger.FAILURE)
                        profile.unregisterQuest(this@Quest)
                    }
                    finish()
                }
            } else {
                finish()
            }
        }
    }

    /**
     * 重置任务
     */
    fun resetQuest() {
        mirrorFuture("Quest:reset") {
            if (QuestEvents.Reset(this@Quest, profile).call().nonCancelled()) {
                template.agent(profile, AgentType.QUEST_RESET, this@Quest).thenAccept {
                    if (it) {
                        tasks.forEach { task ->
                            task.objective.onReset(profile, task)
                            ObjectiveEvents.Reset(task.objective, task, profile).call()
                        }
                        persistentDataContainer.clear()
                    }
                    finish()
                }
            } else {
                finish()
            }
        }
    }
}