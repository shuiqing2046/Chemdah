package ink.ptms.chemdah.core.quest.addon

import ink.ptms.adyeshach.api.AdyeshachAPI
import ink.ptms.adyeshach.api.Hologram
import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.api.ChemdahAPI.chemdahProfile
import ink.ptms.chemdah.api.ChemdahAPI.isChemdahProfileLoaded
import ink.ptms.chemdah.api.ChemdahAPI.nonChemdahProfileLoaded
import ink.ptms.chemdah.api.event.collect.ObjectiveEvents
import ink.ptms.chemdah.api.event.collect.PlayerEvents
import ink.ptms.chemdah.api.event.collect.QuestEvents
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.*
import ink.ptms.chemdah.core.quest.addon.AddonDepend.Companion.isQuestDependCompleted
import ink.ptms.chemdah.core.quest.addon.AddonUI.Companion.ui
import ink.ptms.chemdah.core.quest.addon.data.TrackBeacon
import ink.ptms.chemdah.core.quest.addon.data.TrackLandmark
import ink.ptms.chemdah.core.quest.addon.data.TrackNavigation
import ink.ptms.chemdah.core.quest.addon.data.TrackScoreboard
import ink.ptms.chemdah.core.quest.meta.MetaName.Companion.displayName
import ink.ptms.chemdah.core.quest.selector.InferArea
import ink.ptms.chemdah.module.party.PartySystem.getMembers
import ink.ptms.chemdah.util.conf
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerMoveEvent
import taboolib.common.platform.Schedule
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.adaptCommandSender
import taboolib.common.platform.function.submit
import taboolib.common.platform.function.warning
import taboolib.common.util.asList
import taboolib.common5.Baffle
import taboolib.common5.Coerce
import taboolib.common5.mirrorNow
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.chat.TellrawJson
import taboolib.module.chat.colored
import taboolib.module.nms.sendScoreboard
import taboolib.platform.util.sendLang
import java.util.concurrent.ConcurrentHashMap

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.addon.AddonTrack
 *
 * @author sky
 * @since 2021/3/11 9:05 上午
 */
@Id("track")
@Option(Option.Type.SECTION)
class AddonTrack(config: ConfigurationSection, questContainer: QuestContainer) : Addon(config, questContainer) {

    /**
     * 引导的目的地
     */
    val center = if (config.contains("center")) InferArea.Single(config.getString("center").toString(), false).positions[0] else null
        get() = field?.clone()

    /**
     * 引导开启的提示消息
     * 使用 $ 指向语言文件节点
     */
    val message = config.get("message")?.asList()?.colored() ?: conf.getString("default-track.message").asList().colored()

    /**
     * 记分板中的显示名称与描述
     */
    val name = config.getString("name")?.colored()
    val description = config.get("description")?.asList()?.colored()

    /**
     * 各引导效果
     */
    val beacon = TrackBeacon(config, conf.getConfigurationSection("default-track.beacon"))
    val landmark = TrackLandmark(config, conf.getConfigurationSection("default-track.landmark"))
    val navigation = TrackNavigation(config, conf.getConfigurationSection("default-track.navigation"))
    val scoreboard = TrackScoreboard(config, conf.getConfigurationSection("default-track.scoreboard"))

    companion object {

        private val trackLandmarkHologramMap = ConcurrentHashMap<String, MutableMap<String, Hologram<*>>>()

        private val acceptedQuestsMap = ConcurrentHashMap<String, List<Quest>>()

        private val scoreboardBaffle = Baffle.of(100)

        private val refreshBaffle = Baffle.of(20)

        private val playerBaffle = ConcurrentHashMap<String, MutableMap<String, Baffle>>()

        private val chars = (1..50).map { '黑' + it }

        private val defaultContent by lazy {
            conf.getList("default-track.scoreboard.content").filterNotNull().map { TrackScoreboard.Line(it.asList().colored()) }
        }

        private val defaultMessage by lazy {
            conf.get("default-track.message")?.asList()?.colored() ?: emptyList()
        }

        private val defaultLength by lazy {
            conf.getInt("default-track.scoreboard.length")
        }

        /**
         * 任务允许被追踪
         */
        fun QuestContainer.allowTracked() = when (this) {
            is Template -> track() != null || taskMap.values.any { it.track() != null }
            is Task -> track() != null
            else -> error("out of case")
        }

        /**
         * 任务追踪扩展
         */
        fun QuestContainer.track(): AddonTrack? {
            return when (this) {
                is Template -> addon("track")
                is Task -> addon("track") ?: template.track()
                else -> error("out of case")
            }
        }

        /**
         * 当前任务追踪
         */
        var PlayerProfile.trackQuest: Template?
            set(value) {
                // 当任务不允许追踪时跳过
                if (value != null && !value.allowTracked()) {
                    warning("Quest(${value.path}) not allowed to tracked.")
                    return
                }
                // 唤起事件供外部调用
                if (PlayerEvents.Track(player, this, value ?: trackQuest, value == null).call()) {
                    if (value != null) {
                        persistentDataContainer["quest.track"] = value.id
                    } else {
                        persistentDataContainer.remove("quest.track")
                    }
                }
            }
            get() {
                return persistentDataContainer["quest.track"]?.run { ChemdahAPI.getQuestTemplate(toString()) }
            }

        /**
         * 刷新烽火、寻路以及记分板
         */
        @Schedule(period = 1, async = true)
        internal fun trackTick() {
            Bukkit.getOnlinePlayers().filter { it.isChemdahProfileLoaded }.forEach { player ->
                val chemdahProfile = player.chemdahProfile
                val quest = chemdahProfile.trackQuest ?: return@forEach
                // 缓存任务
                if (refreshBaffle.hasNext(player.name)) {
                    acceptedQuestsMap[player.name] = chemdahProfile.getQuests(openAPI = true)
                }
                // 若任务未接受则追踪任务整体
                if (acceptedQuestsMap[player.name]?.none { it.id == quest.id } == true) {
                    val track = quest.track() ?: return@forEach
                    player.displayTrackBeacon(track)
                    player.displayTrackNavigation(track)
                }
                // 反之追踪任务条目
                else {
                    quest.taskMap.forEach sub@{ (_, task) ->
                        val track = task.track() ?: return@sub
                        // 条目尚未完成
                        if (!task.isCompleted(chemdahProfile) && task.isQuestDependCompleted(player)) {
                            player.displayTrackBeacon(track)
                            player.displayTrackNavigation(track)
                        }
                    }
                }
                // 记分板刷新周期
                if (scoreboardBaffle.hasNext(player.name)) {
                    player.displayTrackScoreboard()
                }
            }
        }

        /**
         * 播放烽火效果
         */
        private fun Player.displayTrackBeacon(trackAddon: AddonTrack) {
            val center = trackAddon.center ?: return
            if (center.world != null && center.world!!.name == world.name && trackAddon.beacon.enable) {
                val distance = center.distance(location)
                if (distance > trackAddon.beacon.distance) {
                    if (trackAddon.beacon.period.hasNext(name)) {
                        saveBaffle("${trackAddon.questContainer.path}.landmark", trackAddon.beacon.period)
                        trackAddon.beacon.display(this, center)
                    }
                }
            }
        }

        /**
         * 播放寻路效果
         */
        private fun Player.displayTrackNavigation(trackAddon: AddonTrack) {
            val center = trackAddon.center ?: return
            val nav = trackAddon.navigation
            if (nav.enable && center.world?.name == world.name && center.distance(location) < nav.distance) {
                when (nav.type) {
                    "POINT" -> {
                        if (nav.pointPeriod.hasNext(name)) {
                            saveBaffle("${trackAddon.questContainer.path}.navigation.point", nav.pointPeriod)
                            nav.displayPoint(this, center)
                        }
                    }
                    "ARROW" -> {
                        if (nav.arrowPeriod.hasNext(name)) {
                            saveBaffle("${trackAddon.questContainer.path}.navigation.arrow", nav.arrowPeriod)
                            nav.displayArrow(this, center)
                        }
                    }
                }
            }
        }

        /**
         * 取消地标效果
         */
        private fun Player.cancelTrackLandmark() {
            if (trackLandmarkHologramMap.containsKey(name)) {
                trackLandmarkHologramMap.remove(name)!!.forEach {
                    it.value.delete()
                }
            }
        }

        /**
         * 创建或更新任务地标
         */
        private fun Player.displayTrackLandmark() {
            if (nonChemdahProfileLoaded) {
                return
            }
            val chemdahProfile = chemdahProfile
            val quest = chemdahProfile.trackQuest ?: return
            // 未接受任务则指向任务本体
            if (chemdahProfile.getQuestById(quest.id) == null) {
                displayTrackLandmark(quest.track() ?: return, quest.path, true)
            }
            // 已接受任务则指向任务条目
            else {
                quest.taskMap.forEach { (_, task) ->
                    // 依赖条目是否完成
                    if (task.isQuestDependCompleted(this)) {
                        displayTrackLandmark(task.track() ?: return@forEach, task.path, !task.isCompleted(chemdahProfile))
                    }
                }
            }
        }

        /**
         * 创建或更新任务地标
         */
        private fun Player.displayTrackLandmark(trackAddon: AddonTrack, id: String, allow: Boolean) {
            val trackCenter = trackAddon.center ?: return
            val hologramMap = trackLandmarkHologramMap.computeIfAbsent(name) { ConcurrentHashMap() }
            // 启用 Landmark 并在相同世界
            if (trackAddon.landmark.enable && trackCenter.world?.name == world.name && allow) {
                mirrorNow("AddonTrack:refreshTrackingLandmark") {
                    val name = trackAddon.name ?: trackAddon.questContainer.displayName()
                    val distance = trackCenter.distance(location)
                    val direction = trackCenter.toVector().subtract(location.toVector()).normalize()
                    val pos = if (distance < trackAddon.landmark.distance) trackCenter else location.add(direction.multiply(trackAddon.landmark.distance))
                    if (hologramMap.containsKey(id)) {
                        hologramMap[id]!!.also { holo ->
                            holo.teleport(pos)
                            holo.update(trackAddon.landmark.content.map {
                                it.replace("{name}", name).replace("{distance}", Coerce.format(distance).toString())
                            })
                        }
                    } else {
                        hologramMap[id] = AdyeshachAPI.createHologram(this, pos, trackAddon.landmark.content.map {
                            it.replace("{name}", name).replace("{distance}", Coerce.format(distance).toString())
                        })
                    }
                }
            } else {
                hologramMap.remove(id)?.delete()
            }
        }

        /**
         * 删除任务追踪
         */
        private fun Player.cancelTrackScoreboard(quest: Template?) {
            if (nonChemdahProfileLoaded || quest == null) {
                return
            }
            // 任务本体活任意子条目启用 Scoreboard 追踪
            if (quest.track()?.scoreboard?.enable == true || quest.taskMap.any { it.value.track()?.scoreboard?.enable == true }) {
                sendScoreboard("")
            }
        }

        /**
         * 创建或刷新任务追踪（Scoreboard）
         */
        private fun Player.displayTrackScoreboard() {
            if (nonChemdahProfileLoaded) {
                return
            }
            mirrorNow("AddonTrack:refreshTrackingScoreboard") {
                val quest = chemdahProfile.trackQuest ?: return@mirrorNow
                if (quest.track()?.scoreboard?.enable == true) {
                    // 尚未接受任务，显示任务总信息
                    val content = if (chemdahProfile.getQuestById(quest.id) == null) {
                        val track = quest.track() ?: return@mirrorNow
                        track.scoreboard.content.flatMap {
                            if (it.isQuestFormat) {
                                it.content.flatMap { contentLine ->
                                    if (contentLine.contains("{description}")) {
                                        val description = track.description ?: quest.ui()?.description ?: emptyList()
                                        description.split(track.scoreboard.length).map { desc -> contentLine.replace("{description}", desc) }
                                    } else {
                                        contentLine.replace("{name}", track.name ?: quest.displayName()).asList()
                                    }
                                }
                            } else {
                                it.content
                            }
                        }
                    } else {
                        (quest.track()?.scoreboard?.content ?: defaultContent).flatMap {
                            if (it.isQuestFormat) {
                                quest.taskMap.flatMap { (_, task) ->
                                    val taskTrack = task.track()
                                    if (taskTrack != null && !task.isCompleted(chemdahProfile) && task.isQuestDependCompleted(this)) {
                                        it.content.flatMap { contentLine ->
                                            if (contentLine.contains("{description}")) {
                                                val description = taskTrack.description ?: quest.ui()?.description ?: emptyList()
                                                val size = quest.track()?.scoreboard?.length ?: defaultLength
                                                description.split(size).map { d -> contentLine.replace("{description}", d) }
                                            } else {
                                                contentLine.replace("{name}", taskTrack.name ?: task.displayName()).asList()
                                            }
                                        }
                                    } else {
                                        emptyList()
                                    }
                                }
                            } else {
                                it.content
                            }
                        }
                    }
                    if (content.size > 2) {
                        sendScoreboard(*content.colored().mapIndexed { index, s -> "§${chars[index]}$s" }.toTypedArray())
                    } else {
                        cancelTrackScoreboard(quest)
                    }
                }
            }
        }

        /**
         * 修复进入服务器无法显示任务追踪的漏洞
         * 不知道原因
         */
        @SubscribeEvent
        internal fun onSelect(e: PlayerEvents.Selected) {
            submit(delay = 40) {
                if (e.playerProfile.trackQuest != null) {
                    e.player.cancelTrackLandmark()
                    e.player.cancelTrackScoreboard(e.playerProfile.trackQuest!!)
                    e.player.displayTrackLandmark()
                    e.player.displayTrackScoreboard()
                }
            }
        }

        /**
         * 条目完成时刷新任务追踪
         * 已完成的条目不再显示于记分板中
         */
        @SubscribeEvent
        internal fun onComplete(e: ObjectiveEvents.Complete.Post) {
            e.quest.getMembers(self = true).forEach {
                if (it.chemdahProfile.trackQuest == e.task.template) {
                    it.displayTrackLandmark()
                    it.displayTrackScoreboard()
                }
            }
        }

        /**
         * 任务注销时取消任务追踪
         */
        @SubscribeEvent
        internal fun onUnregistered(e: QuestEvents.Unregistered) {
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
         * 任务注册时刷新任务追踪
         * 总内容替换为子内容显示
         */
        @SubscribeEvent
        internal fun onRegistered(e: QuestEvents.Registered) {
            if (e.playerProfile.trackQuest == e.quest.template) {
                e.playerProfile.player.displayTrackLandmark()
                e.playerProfile.player.displayTrackScoreboard()
            }
        }

        /**
         * 切换追踪状态时刷新或取消任务追踪
         * 并给予相关提示
         */
        @SubscribeEvent(priority = EventPriority.MONITOR, ignoreCancelled = true)
        internal fun onTrack(e: PlayerEvents.Track) {
            if (e.cancel) {
                e.player.cancelTrackLandmark()
                e.player.cancelTrackScoreboard(e.trackingQuest)
                e.player.sendLang("track-cancel")
            } else {
                e.player.cancelTrackLandmark()
                e.player.cancelTrackScoreboard(e.trackingQuest)
                submit(delay = 1) {
                    e.player.displayTrackLandmark()
                    e.player.displayTrackScoreboard()
                    // 发送追踪信息
                    (e.trackingQuest!!.track()?.message ?: defaultMessage).forEach { message ->
                        if (message.startsWith('$')) {
                            e.player.sendLang(message.substring(1))
                        } else {
                            TellrawJson().append(message.replace("{name}", e.trackingQuest.displayName()))
                                .hoverText(message.replace("{name}", e.trackingQuest.displayName()))
                                .runCommand("/ChemdahTrackCancel")
                                .sendTo(adaptCommandSender(e.player))
                        }
                    }
                }
            }
        }

        /**
         * 玩家移动时刷新 Navigation 导航
         */
        @SubscribeEvent
        internal fun onMove(e: PlayerMoveEvent) {
            if (e.from.toVector() != e.to!!.toVector()) {
                e.player.displayTrackLandmark()
            }
        }

        @SubscribeEvent
        internal fun onQuit(e: PlayerEvents.Released) {
            trackLandmarkHologramMap.remove(e.player.name)
            acceptedQuestsMap.remove(e.player.name)
            scoreboardBaffle.reset(e.player.name)
            refreshBaffle.reset(e.player.name)
            playerBaffle.remove(e.player.name)?.forEach {
                it.value.reset(e.player.name)
            }
            e.player.cancelTrackLandmark()
        }

        @SubscribeEvent
        internal fun onCommand(e: PlayerCommandPreprocessEvent) {
            if (e.message.equals("/ChemdahTrackCancel", true)) {
                e.isCancelled = true
                e.player.chemdahProfile.trackQuest = null
            }
        }

        /**
         * 记录玩家的 Baffle 对象，用于在离线时释放缓存
         */
        private fun Player.saveBaffle(node: String, baffle: Baffle) {
            val map = playerBaffle.computeIfAbsent(name) { HashMap() }
            if (!map.containsKey(node)) {
                map[node] = baffle
            }
        }

        private fun List<String>.split(size: Int) = colored().flatMap { line ->
            if (line.length > size) {
                val arr = ArrayList<String>()
                var s = line
                while (s.length > size) {
                    val c = s.substring(0, size)
                    val i = c.lastIndexOf("§")
                    arr.add(c)
                    s = if (i != -1 && i + 2 < c.length) {
                        s.substring(i, i + 2) + s.substring(size)
                    } else {
                        s.substring(size)
                    }
                }
                arr.add(s)
                arr
            } else {
                line.asList()
            }
        }
    }
}