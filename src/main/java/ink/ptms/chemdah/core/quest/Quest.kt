package ink.ptms.chemdah.core.quest

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.api.event.QuestEvents
import ink.ptms.chemdah.core.DataContainer
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.meta.MetaControl.Companion.control
import ink.ptms.chemdah.core.quest.meta.MetaTimeout.Companion.isTimeout
import ink.ptms.chemdah.core.quest.option.MetaControl
import ink.ptms.chemdah.util.mirrorFuture

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.Quest
 *
 * @author sky
 * @since 2021/3/2 12:03 上午
 */
class Quest(val id: String, val profile: PlayerProfile) {

    val template: Template
        get() = ChemdahAPI.getQuestTemplate(id)!!

    val isValid: Boolean
        get() = ChemdahAPI.getQuestTemplate(id) != null

    val isCompleted: Boolean
        get() = isValid && template.tasks.all { it.value.objective.hasCompletedSignature(profile, it.value) }

    val tasks: Collection<Task>
        get() = template.tasks.values

    val startTime: Long
        get() = persistentDataContainer["start", 0L].toLong()

    val isTimeout: Boolean
        get() = template.isTimeout(startTime)

    val persistentDataContainer = DataContainer()

    init {
        persistentDataContainer.put("start", System.currentTimeMillis())
        profile.persistentDataContainer.remove("quest.complete.$id")
    }

    /**
     * 检查任务的所有条目
     * 当所有条目均已完成时任务完成
     */
    fun checkComplete() {
        mirrorFuture("Quest:checkComplete") {
            template.checkReset(profile).thenAccept { reset ->
                if (reset) {
                    resetQuest()
                    finish()
                } else {
                    if (tasks.all { it.objective.hasCompletedSignature(profile, it) } && QuestEvents.Complete(template, profile).call().nonCancelled()) {
                        template.agent(profile, AgentType.QUEST_COMPLETE).thenAccept {
                            if (it) {
                                template.control().signature(profile, MetaControl.ControlRepeat.Type.COMPLETE)
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
     * 放弃任务
     */
    fun failureQuest() {
        mirrorFuture("Quest:failure") {
            if (QuestEvents.Failure(template, profile).call().nonCancelled()) {
                template.agent(profile, AgentType.QUEST_FAILURE).thenAccept {
                    if (it) {
                        template.control().signature(profile, MetaControl.ControlRepeat.Type.FAILURE)
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
            if (QuestEvents.Reset(template, profile).call().nonCancelled()) {
                template.agent(profile, AgentType.QUEST_RESET).thenAccept {
                    if (it) {
                        tasks.forEach { task -> task.objective.onReset(profile, task) }
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