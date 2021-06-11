package ink.ptms.chemdah.core.quest.addon

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.api.ChemdahAPI.chemdahProfile
import ink.ptms.chemdah.api.ChemdahAPI.isChemdahProfileLoaded
import ink.ptms.chemdah.api.ChemdahAPI.nonChemdahProfileLoaded
import ink.ptms.chemdah.api.HologramAPI
import ink.ptms.chemdah.api.HologramAPI.createHologram
import ink.ptms.chemdah.api.event.collect.ObjectiveEvents
import ink.ptms.chemdah.api.event.collect.PlayerEvents
import ink.ptms.chemdah.api.event.collect.QuestEvents
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.*
import ink.ptms.chemdah.core.quest.addon.AddonUI.Companion.ui
import ink.ptms.chemdah.core.quest.meta.MetaName.Companion.displayName
import ink.ptms.chemdah.core.quest.selector.InferArea
import ink.ptms.chemdah.module.party.PartySystem.getMembers
import ink.ptms.chemdah.util.*
import io.izzel.taboolib.kotlin.Tasks
import io.izzel.taboolib.kotlin.navigation.Navigation
import io.izzel.taboolib.kotlin.navigation.pathfinder.NodeEntity
import io.izzel.taboolib.kotlin.sendScoreboard
import io.izzel.taboolib.module.inject.PlayerContainer
import io.izzel.taboolib.module.inject.TListener
import io.izzel.taboolib.module.inject.TSchedule
import io.izzel.taboolib.module.locale.TLocale
import io.izzel.taboolib.module.tellraw.TellrawJson
import io.izzel.taboolib.util.Baffle
import io.izzel.taboolib.util.Coerce
import io.izzel.taboolib.util.lite.Effects
import org.bukkit.Bukkit
import org.bukkit.Particle
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
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

    class ScoreboardContent(val content: List<String>) {

        val isQuestFormat = content.size > 1
        val value = content[0]
    }

    val center = if (config.contains("center")) InferArea.Single(config.getString("center").toString(), false).positions[0] else null
        get() = field?.clone()

    val message = config.get("message")?.asList()?.colored() ?: defaultMessage

    val name = config.getString("name")?.colored()
    val description = config.get("description")?.asList()?.colored()

    val mark = config.getBoolean("mark", conf.getBoolean("default-track.mark.value"))
    val markType = try {
        Particle.valueOf(config.getString("mark-option.type", conf.getString("default-track.mark.type"))!!.toUpperCase())
    } catch (ex: Throwable) {
        Particle.VILLAGER_HAPPY
    }
    val markSize = config.getDouble("mark-option.size", conf.getDouble("default-track.mark.size"))
    val markCount = config.getInt("mark-option.count", conf.getInt("default-track.mark.count"))
    val markDistanceMin = config.getDouble("mark-option.distance.min", conf.getDouble("default-track.mark.distance.min"))
    val markDistanceMax = config.getDouble("mark-option.distance.max", conf.getDouble("default-track.mark.distance.max"))
    val markPeriod = Baffle.of(config.getInt("mark-option.period", conf.getInt("default-track.mark.period")))

    val navigation = config.getBoolean("navigation", conf.getBoolean("default-track.navigation.value"))
    val navigationSync = config.getBoolean("navigation-option.sync", conf.getBoolean("default-track.navigation.sync"))
    val navigationType = try {
        Particle.valueOf(config.getString("navigation-option.type", conf.getString("default-track.navigation.type"))!!.toUpperCase())
    } catch (ex: Throwable) {
        Particle.END_ROD
    }
    val navigationSizeX = config.getDouble("navigation-option.size.x", conf.getDouble("default-track.navigation.size.x"))
    val navigationSizeY = config.getDouble("navigation-option.size.y", conf.getDouble("default-track.navigation.size.y"))
    val navigationCount = config.getInt("navigation-option.count", conf.getInt("default-track.navigation.count"))
    val navigationContent = config.get("navigation-option.content", conf.get("default-track.navigation.content"))?.asList()?.colored() ?: emptyList()
    val navigationDistanceMin = config.getDouble("navigation-option.distance.min", conf.getDouble("default-track.navigation.distance.min"))
    val navigationDistanceMax = config.getDouble("navigation-option.distance.max", conf.getDouble("default-track.navigation.distance.max"))
    val navigationPeriod = Baffle.of(config.getInt("navigation-option.period", conf.getInt("default-track.navigation.period")))

    val scoreboard = config.getBoolean("scoreboard", conf.getBoolean("default-track.scoreboard.value"))
    val scoreboardLength = config.getInt("scoreboard-length", defaultLength)
    val scoreboardContent = config.getList("scoreboard-content")?.run {
        filterNotNull().map {
            ScoreboardContent(it.asList().colored())
        }
    } ?: defaultContent

    @TListener
    companion object : Listener {

        @PlayerContainer
        private val trackNavigationHologramMap = ConcurrentHashMap<String, MutableMap<String, HologramAPI.Hologram<*>>>()

        @PlayerContainer
        private val acceptedQuestsMap = ConcurrentHashMap<String, List<Quest>>()

        @PlayerContainer
        private val scoreboardBaffle = Baffle.of(100)

        @PlayerContainer
        private val refreshBaffle = Baffle.of(20)

        private val playerBaffle = ConcurrentHashMap<String, MutableMap<String, Baffle>>()

        private val chars = (1..50).map { '黑' + it }

        private val defaultContent by lazy {
            conf.getList("default-track.scoreboard.content")?.run {
                filterNotNull().map {
                    ScoreboardContent(it.asList().colored())
                }
            } ?: emptyList()
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
                PlayerEvents.Track(player, this, value ?: trackQuest, value == null).call().nonCancelled {
                    if (value != null) {
                        persistentDataContainer["quest.track"] = value.id
                    } else {
                        persistentDataContainer.remove("quest.track")
                    }
                }
            }
            get() = persistentDataContainer["quest.track"]?.run { ChemdahAPI.getQuestTemplate(toString()) }

        /**
         * 地标及导航追踪
         */
        @TSchedule(period = 1, async = true)
        private fun trackTick() {
            Bukkit.getOnlinePlayers().filter { it.isChemdahProfileLoaded }.forEach { player ->
                val chemdahProfile = player.chemdahProfile
                val quest = chemdahProfile.trackQuest ?: return@forEach
                // 缓存任务
                if (refreshBaffle.hasNext(player.name)) {
                    acceptedQuestsMap[player.name] = chemdahProfile.getQuests(openAPI = true)
                }
                // 若任务未接受则追踪任务整体
                if (acceptedQuestsMap[player.name]?.any { it.id == quest.id } == true) {
                    val track = quest.track() ?: return@forEach
                    player.trackTickMark(track)
                    player.trackTickNavigation(track)
                }
                // 反之追踪任务条目
                else {
                    quest.taskMap.forEach sub@{ (_, task) ->
                        val track = task.track() ?: return@sub
                        // 条目尚未完成
                        if (!task.isCompleted(chemdahProfile)) {
                            player.trackTickMark(track)
                            player.trackTickNavigation(track)
                        }
                    }
                }
                // 记分板刷新周期
                if (scoreboardBaffle.hasNext(player.name)) {
                    player.refreshTrackingScoreboard()
                }
            }
        }

        private fun Player.signatureBaffle(node: String, baffle: Baffle) {
            val map = playerBaffle.computeIfAbsent(name) { HashMap() }
            if (!map.containsKey(node)) {
                map[node] = baffle
            }
        }

        private fun Player.trackTickMark(trackAddon: AddonTrack) {
            val center = trackAddon.center ?: return
            if (center.world.name == world.name) {
                val distance = center.distance(location)
                if (distance > trackAddon.markDistanceMin) {
                    if (trackAddon.mark && trackAddon.markPeriod.hasNext(name)) {
                        signatureBaffle("${trackAddon.questContainer.path}.mark", trackAddon.markPeriod)
                        val direction = center.toVector().subtract(location.toVector()).normalize()
                        val pos = location.add(direction.multiply(distance.coerceAtMost(trackAddon.markDistanceMax)))
                        Effects.create(trackAddon.markType, pos)
                            .offset(doubleArrayOf(trackAddon.markSize, 128.0, trackAddon.markSize))
                            .count(trackAddon.markCount)
                            .player(this)
                            .play()
                    }
                }
            }
        }

        private fun Player.trackTickNavigation(trackAddon: AddonTrack) {
            val center = trackAddon.center ?: return
            if (center.world.name == world.name) {
                val distance = center.distance(location)
                if (distance < trackAddon.navigationDistanceMax) {
                    if (trackAddon.navigation && trackAddon.navigationPeriod.hasNext(name)) {
                        signatureBaffle("${trackAddon.questContainer.path}.navigation", trackAddon.navigationPeriod)
                        Tasks.task(!trackAddon.navigationSync) {
                            mirrorFuture("AddonTrack:trackTickNavigation:${if (trackAddon.navigationSync) "sync" else "async"}") {
                                val pathFinder = Navigation.create(NodeEntity(location, 2.0, 1.0, canOpenDoors = true, canPassDoors = true))
                                val path = pathFinder.findPath(center, distance = trackAddon.navigationDistanceMax.toFloat())
                                path?.nodes?.forEachIndexed { index, node ->
                                    Tasks.delay(index.toLong()) {
                                        Effects.create(trackAddon.navigationType, node.asBlockPos().toLocation(center.world).toCenter())
                                            .offset(doubleArrayOf(trackAddon.navigationSizeX, trackAddon.navigationSizeY, trackAddon.navigationSizeX))
                                            .count(trackAddon.navigationCount)
                                            .player(this@trackTickNavigation)
                                            .play()
                                    }
                                }
                                finish()
                            }
                        }
                    }
                }
            }
        }

        /**
         * 删除任务追踪（Navigation）
         */
        fun Player.cancelTrackingNavigation() {
            if (trackNavigationHologramMap.containsKey(name)) {
                trackNavigationHologramMap.remove(name)!!.forEach {
                    it.value.delete()
                }
            }
        }

        /**
         * 创建或更新任务追踪（Navigation）
         */
        fun Player.refreshTrackingNavigation() {
            if (nonChemdahProfileLoaded) {
                return
            }
            val chemdahProfile = chemdahProfile
            val quest = chemdahProfile.trackQuest ?: return
            // 未接受任务
            if (chemdahProfile.getQuestById(quest.id) == null) {
                refreshTrackingNavigation(quest.track() ?: return, quest.path, true)
            } else {
                quest.taskMap.forEach { (_, task) ->
                    refreshTrackingNavigation(task.track() ?: return@forEach, task.path, !task.isCompleted(chemdahProfile))
                }
            }
        }

        private fun Player.refreshTrackingNavigation(trackAddon: AddonTrack, id: String, allow: Boolean) {
            val trackCenter = trackAddon.center ?: return
            val hologramMap = trackNavigationHologramMap.computeIfAbsent(name) { ConcurrentHashMap() }
            // 启用 Navigation 并在相同世界
            if (trackAddon.navigation && trackCenter.world.name == world.name && allow) {
                mirrorFuture("AddonTrack:refreshTrackingNavigation") {
                    val name = trackAddon.name ?: trackAddon.questContainer.displayName()
                    val distance = trackCenter.distance(location)
                    val direction = trackCenter.toVector().subtract(location.toVector()).normalize()
                    val pos = if (distance < trackAddon.navigationDistanceMin) {
                        trackCenter
                    } else {
                        location.add(direction.multiply(trackAddon.navigationDistanceMin))
                    }
                    if (hologramMap.containsKey(id)) {
                        hologramMap[id]!!.also { holo ->
                            holo.teleport(pos)
                            holo.update(trackAddon.navigationContent.map {
                                it.replace("{name}", name).replace("{distance}", Coerce.format(distance).toString())
                            })
                        }
                    } else {
                        hologramMap[id] = createHologram(pos, trackAddon.navigationContent.map {
                            it.replace("{name}", name).replace("{distance}", Coerce.format(distance).toString())
                        })
                    }
                    finish()
                }
            } else {
                hologramMap.remove(id)?.delete()
            }
        }

        /**
         * 删除任务追踪
         */
        fun Player.cancelTrackingScoreboard(quest: Template?) {
            if (nonChemdahProfileLoaded || quest == null) {
                return
            }
            if (chemdahProfile.getQuestById(quest.id) == null) {
                // 启用 Scoreboard 追踪
                if (quest.track()?.scoreboard == true) {
                    sendScoreboard("")
                }
            } else {
                // 任意子条目启用 Scoreboard 追踪
                if (quest.taskMap.any { it.value.track()?.scoreboard == true }) {
                    sendScoreboard("")
                }
            }
        }

        /**
         * 创建或刷新任务追踪（Scoreboard）
         */
        fun Player.refreshTrackingScoreboard() {
            if (nonChemdahProfileLoaded) {
                return
            }
            mirrorFuture("AddonTrack:refreshTrackingScoreboard") {
                val quest = chemdahProfile.trackQuest ?: return@mirrorFuture
                // 尚未接受任务，显示任务总信息
                val content = if (chemdahProfile.getQuestById(quest.id) == null) {
                    val track = quest.track() ?: return@mirrorFuture
                    track.scoreboardContent.flatMap {
                        if (it.isQuestFormat) {
                            it.content.flatMap { contentLine ->
                                if (contentLine.contains("{description}")) {
                                    val description = track.description ?: quest.ui()?.description ?: emptyList()
                                    description.split(track.scoreboardLength).map { descriptionLine -> contentLine.replace("{description}", descriptionLine) }
                                } else {
                                    contentLine.replace("{name}", track.name ?: quest.displayName()).asList()
                                }
                            }
                        } else {
                            it.content
                        }
                    }
                } else {
                    (quest.track()?.scoreboardContent ?: defaultContent).flatMap {
                        if (it.isQuestFormat) {
                            quest.taskMap.flatMap { (_, task) ->
                                val taskTrack = task.track()
                                if (taskTrack != null && !task.isCompleted(chemdahProfile)) {
                                    it.content.flatMap { contentLine ->
                                        if (contentLine.contains("{description}")) {
                                            val description = taskTrack.description ?: quest.ui()?.description ?: emptyList()
                                            val size = quest.track()?.scoreboardLength ?: defaultLength
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
                    cancelTrackingScoreboard(quest)
                }
                finish()
            }
        }

        /**
         * 修复进入服务器无法显示任务追踪的漏洞
         * 不知道原因
         */
        @EventHandler
        private fun onSelect(e: PlayerEvents.Selected) {
            Tasks.delay(40) {
                if (e.playerProfile.trackQuest != null) {
                    e.player.cancelTrackingNavigation()
                    e.player.cancelTrackingScoreboard(e.playerProfile.trackQuest!!)
                    e.player.refreshTrackingNavigation()
                    e.player.refreshTrackingScoreboard()
                }
            }
        }

        /**
         * 条目完成时刷新任务追踪
         * 已完成的条目不再显示于记分板中
         */
        @EventHandler
        private fun onComplete(e: ObjectiveEvents.Complete.Post) {
            e.quest.getMembers(self = true).forEach {
                if (it.chemdahProfile.trackQuest == e.task.template) {
                    it.refreshTrackingNavigation()
                    it.refreshTrackingScoreboard()
                }
            }
        }

        /**
         * 任务注销时取消任务追踪
         */
        @EventHandler
        private fun onUnregistered(e: QuestEvents.Unregistered) {
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
        @EventHandler
        private fun onRegistered(e: QuestEvents.Registered) {
            if (e.playerProfile.trackQuest == e.quest.template) {
                e.playerProfile.player.refreshTrackingNavigation()
                e.playerProfile.player.refreshTrackingScoreboard()
            }
        }

        /**
         * 切换追踪状态时刷新或取消任务追踪
         * 并给予相关提示
         */
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        private fun onTrack(e: PlayerEvents.Track) {
            if (e.cancel) {
                e.player.cancelTrackingNavigation()
                e.player.cancelTrackingScoreboard(e.trackingQuest)
                TLocale.sendTo(e.player, "track-cancel")
            } else {
                e.player.cancelTrackingNavigation()
                e.player.cancelTrackingScoreboard(e.trackingQuest)
                Tasks.delay(1) {
                    e.player.refreshTrackingNavigation()
                    e.player.refreshTrackingScoreboard()
                    (e.trackingQuest!!.track()?.message ?: defaultMessage).forEach { message ->
                        TellrawJson.create().append(message.replace("{name}", e.trackingQuest.displayName()))
                            .hoverText(message.replace("{name}", e.trackingQuest.displayName()))
                            .clickCommand("/ChemdahTrackCancel")
                            .send(e.player)
                    }
                }
            }
        }

        /**
         * 玩家移动时刷新 Navigation 导航
         */
        @EventHandler
        private fun onMove(e: PlayerMoveEvent) {
            if (e.from.toVector() != e.to.toVector()) {
                e.player.refreshTrackingNavigation()
            }
        }

        @EventHandler
        private fun onCommand(e: PlayerCommandPreprocessEvent) {
            if (e.message.equals("/ChemdahTrackCancel", true)) {
                e.isCancelled = true
                e.player.chemdahProfile.trackQuest = null
            }
        }

        @EventHandler
        private fun onQuit(e: PlayerQuitEvent) {
            playerBaffle.remove(e.player.name)?.forEach {
                it.value.reset(e.player.name)
            }
            e.player.cancelTrackingNavigation()
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