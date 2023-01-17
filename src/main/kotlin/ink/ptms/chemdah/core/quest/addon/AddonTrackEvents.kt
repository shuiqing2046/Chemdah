package ink.ptms.chemdah.core.quest.addon

import ink.ptms.chemdah.api.ChemdahAPI.chemdahProfile
import ink.ptms.chemdah.api.ChemdahAPI.isChemdahProfileLoaded
import ink.ptms.chemdah.api.event.collect.ObjectiveEvents
import ink.ptms.chemdah.api.event.collect.PlayerEvents
import ink.ptms.chemdah.api.event.collect.QuestEvents
import ink.ptms.chemdah.core.quest.Quest
import ink.ptms.chemdah.core.quest.addon.AddonDepend.Companion.isQuestDependCompleted
import ink.ptms.chemdah.core.quest.addon.AddonTrack.Companion.removeLandmarkTracker
import ink.ptms.chemdah.core.quest.addon.AddonTrack.Companion.removeScoreboardTracker
import ink.ptms.chemdah.core.quest.addon.AddonTrack.Companion.sendBeaconTracker
import ink.ptms.chemdah.core.quest.addon.AddonTrack.Companion.sendNavigationTracker
import ink.ptms.chemdah.core.quest.addon.AddonTrack.Companion.track
import ink.ptms.chemdah.core.quest.addon.AddonTrack.Companion.trackQuest
import ink.ptms.chemdah.core.quest.addon.AddonTrack.Companion.updateLandmarkTracker
import ink.ptms.chemdah.core.quest.addon.AddonTrack.Companion.updateScoreboardTracker
import ink.ptms.chemdah.core.quest.meta.MetaName.Companion.displayName
import ink.ptms.chemdah.module.party.PartySystem.getMembers
import ink.ptms.chemdah.util.replace
import org.bukkit.Bukkit
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerMoveEvent
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Schedule
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.adaptCommandSender
import taboolib.common.platform.function.submit
import taboolib.common.platform.function.submitAsync
import taboolib.common5.Baffle
import taboolib.module.chat.TellrawJson
import taboolib.platform.util.isMovement
import taboolib.platform.util.sendLang
import java.util.concurrent.ConcurrentHashMap

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.addon.AddonTrackEvents
 *
 * @author 坏黑
 * @since 2023/1/17 00:32
 */
object AddonTrackEvents {

    /** 玩家爱接受任务缓存 */
    val acceptedQuestsMap = ConcurrentHashMap<String, List<Quest>>()

    /** 记分板更新阻断 */
    val scoreboardCounter = Baffle.of(100)

    /** 任务刷新阻断 */
    val refreshCounter = Baffle.of(20)

    /**
     * 定时刷新导航追踪器
     */
    @Awake(LifeCycle.ENABLE)
    @Schedule(period = 40, async = true)
    fun trackerUpdate40() {
        Bukkit.getOnlinePlayers().filter { it.isChemdahProfileLoaded }.forEach { it.updateLandmarkTracker() }
    }

    /**
     * 刷新记分板、信标、导航追踪器
     */
    @Schedule(period = 1, async = true)
    fun trackerUpdate() {
        Bukkit.getOnlinePlayers().filter { it.isChemdahProfileLoaded }.forEach { player ->
            val chemdahProfile = player.chemdahProfile
            // 获取追踪任务
            val trackQuest = chemdahProfile.trackQuest ?: return@forEach
            // 缓存任务
            if (refreshCounter.hasNext(player.name)) {
                acceptedQuestsMap[player.name] = chemdahProfile.getQuests(openAPI = true)
            }
            // 发送记分板追踪器
            if (scoreboardCounter.hasNext(player.name)) {
                player.updateScoreboardTracker()
            }
            // 未接受任务 -> 追踪任务整体
            if (acceptedQuestsMap[player.name]?.none { it.id == trackQuest.id } == true) {
                val track = trackQuest.track() ?: return@forEach
                // 发送信标追踪器
                player.sendBeaconTracker(track)
                // 发送导航追踪器
                player.sendNavigationTracker(track)
            }
            // 已接受任务 -> 追踪任务所有条目
            else {
                trackQuest.taskMap.forEach sub@{ (_, task) ->
                    val track = task.track() ?: return@sub
                    // 条目未完成 && 条目依赖已完成
                    if (!task.isCompleted(chemdahProfile) && task.isQuestDependCompleted(player)) {
                        // 发送信标追踪器
                        if (PlayerEvents.TrackTask(player, chemdahProfile, task, PlayerEvents.TrackTask.Type.BEACON).call()) {
                            player.sendBeaconTracker(track)
                        }
                        // 发送导航追踪器
                        if (PlayerEvents.TrackTask(player, chemdahProfile, task, PlayerEvents.TrackTask.Type.NAVIGATION).call()) {
                            player.sendNavigationTracker(track)
                        }
                    }
                }
            }
        }
    }

    /**
     * 任务注册时更新任务追踪（追踪任务 -> 追踪条目）
     * 因为信标和导航是实时更新的，所以不需要手动刷新
     */
    @SubscribeEvent
    fun onRegistered(e: QuestEvents.Registered) {
        // 注册的任务是正在追踪的任务
        if (e.playerProfile.trackQuest == e.quest.template) {
            // 更新地标追踪器
            e.playerProfile.player.updateLandmarkTracker()
            // 更新记分板追踪器
            e.playerProfile.player.updateScoreboardTracker()
        }
    }

    /**
     * 任务注销时删除任务追踪
     */
    @SubscribeEvent
    fun onUnregistered(e: QuestEvents.Unregistered) {
        // 如果我在追踪这个任务
        if (e.playerProfile.trackQuest == e.quest.template) {
            // 我和我的队友都会取消这个任务的追踪
            e.quest.getMembers(self = true).forEach {
                if (it.chemdahProfile.trackQuest == e.quest.template) {
                    it.chemdahProfile.trackQuest = null
                }
            }
        }
    }

    /**
     * 条目更新刷新任务追踪（用于更新记分板进度）
     */
    @SubscribeEvent
    fun onContinue(e: ObjectiveEvents.Continue.Post) {
        // 我和我的队友都会更新这个任务的追踪
        e.quest.getMembers(self = true).forEach {
            if (it.chemdahProfile.trackQuest == e.task.template) {
                // 更新记分板 -> 更新进度
                it.updateScoreboardTracker()
            }
        }
    }

    /**
     * 条目完成时刷新任务追踪（用于回收地标和记分板条目）
     */
    @SubscribeEvent
    fun onComplete(e: ObjectiveEvents.Complete.Post) {
        e.quest.getMembers(self = true).forEach {
            if (it.chemdahProfile.trackQuest == e.task.template) {
                // 更新地标 -> 回收全息
                it.updateLandmarkTracker()
                // 更新记分板 -> 回收条目
                it.updateScoreboardTracker()
            }
        }
    }

    /**
     * 修复进入服务器无法显示任务追踪的漏洞
     * 不知道什么原因
     */
    @SubscribeEvent
    fun onSelect(e: PlayerEvents.Selected) {
        submit(delay = 40) {
            // 获取追踪任务
            val trackQuest = e.playerProfile.trackQuest
            if (trackQuest != null) {
                // 删除追踪器
                e.player.removeLandmarkTracker()
                e.player.removeScoreboardTracker(trackQuest)
                // 发送追踪器
                e.player.updateLandmarkTracker()
                e.player.updateScoreboardTracker()
            }
        }
    }

    /**
     * 切换追踪状态时刷新或取消任务追踪，并给予相关提示
     */
    @SubscribeEvent(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onTrack(e: PlayerEvents.Track) {
        // 删除地标追踪器
        e.player.removeLandmarkTracker()
        // 删除记分板追踪器
        e.player.removeScoreboardTracker(e.trackingQuest)
        // 取消追踪
        if (e.cancel) {
            e.player.sendLang("track-cancel")
        } else {
            // 推迟到下一个游戏刻执行
            submit(delay = 1) {
                // 创建地标追踪器
                e.player.updateLandmarkTracker()
                // 更新记分板追踪器（因为记分板不需要创建）
                e.player.updateScoreboardTracker()
                // 发送追踪信息
                (e.trackingQuest!!.track()?.message ?: AddonTrack.defaultMessage).forEach { message ->
                    // 指向语言文件
                    if (message.startsWith('$')) {
                        e.player.sendLang(message.substring(1))
                    } else {
                        // 获取任务名称
                        val displayName = e.trackingQuest.track()?.name ?: e.trackingQuest.displayName()
                        // 构建 Tellraw 信息
                        TellrawJson().append(message.replace("name" to displayName))
                            .hoverText(message.replace("name" to displayName))
                            .runCommand("/ChemdahTrackCancel")
                            .sendTo(adaptCommandSender(e.player))
                    }
                }
            }
        }
    }

    /**
     * 移动时刷新地标追踪器
     */
    @SubscribeEvent
    fun onMove(e: PlayerMoveEvent) {
        if (e.isMovement()) {
            submitAsync { e.player.updateLandmarkTracker() }
        }
    }

    /**
     * 取消追踪命令
     */
    @Suppress("SpellCheckingInspection")
    @SubscribeEvent
    fun onCommand(e: PlayerCommandPreprocessEvent) {
        if (e.message.equals("/chemdahtrackcancel", true)) {
            e.isCancelled = true
            e.player.chemdahProfile.trackQuest = null
        }
    }

    /**
     * 玩家离线时释放资源
     */
    @SubscribeEvent
    fun onQuit(e: PlayerEvents.Released) {
        // 删除全息（因为全息依赖 Adyeshach）
        e.player.removeLandmarkTracker()
        // 释放资源
        AddonTrack.landmarkHologramMap.remove(e.player.name)
        AddonTrack.releaseCounterMap.remove(e.player.name)?.forEach { it.value.reset(e.player.name) }
        acceptedQuestsMap.remove(e.player.name)
        scoreboardCounter.reset(e.player.name)
        refreshCounter.reset(e.player.name)
    }
}