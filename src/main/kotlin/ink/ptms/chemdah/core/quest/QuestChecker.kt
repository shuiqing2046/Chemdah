package ink.ptms.chemdah.core.quest

import ink.ptms.chemdah.api.ChemdahAPI.chemdahProfile
import ink.ptms.chemdah.api.ChemdahAPI.isChemdahProfileLoaded
import ink.ptms.chemdah.core.quest.objective.bukkit.EMPTY_EVENT
import org.bukkit.Bukkit
import taboolib.common.platform.Schedule
import taboolib.common.platform.function.console
import taboolib.common.platform.function.warning
import taboolib.module.lang.sendLang
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.QuestChecker
 *
 * @author 坏黑
 * @since 2022/8/31 23:05
 */
object QuestChecker {

    @Schedule(period = 20, async = true)
    fun checkQuestNow() {
        Bukkit.getOnlinePlayers().filter { it.isChemdahProfileLoaded }.forEach { player ->
            player.chemdahProfile.also { profile ->
                // 检测所有有效任务
                profile.getQuests().forEach self@{ quest ->
                    // 锁定状态
                    if (quest.lock) {
                        console().sendLang("console-quest-locked", player.name, quest.id)
                        return@self
                    } else {
                        // 锁定任务避免下次检查时重复执行
                        quest.lock = true
                    }
                    // 检测超时
                    if (quest.isTimeout) {
                        quest.failQuestFuture().thenAccept {
                            // 解锁
                            quest.lock = false
                        }
                    } else {
                        // 检查条目自动完成
                        CompletableFuture.allOf(*quest.tasks.map { task ->
                            // 处理需要自动检查的任务类型
                            if (task.objective.isTickable) {
                                QuestLoader.handleTask(profile, task, quest, EMPTY_EVENT)
                                CompletableFuture.completedFuture(null)
                            } else {
                                task.objective.checkComplete(profile, task, quest)
                            }
                        }.toTypedArray()).thenAccept {
                            // 检查任务自动完成
                            quest.checkCompleteFuture().thenAccept {
                                // 解锁
                                quest.lock = false
                            }
                        }
                    }
                }
            }
        }
    }
}