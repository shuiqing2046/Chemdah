package ink.ptms.chemdah.core.quest.addon

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.api.ChemdahAPI.chemdahProfile
import ink.ptms.chemdah.api.HologramAPI
import ink.ptms.chemdah.api.HologramAPI.createHologram
import ink.ptms.chemdah.api.event.ObjectiveEvent
import ink.ptms.chemdah.api.event.PlayerEvent
import ink.ptms.chemdah.api.event.QuestEvent
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Id
import ink.ptms.chemdah.core.quest.QuestContainer
import ink.ptms.chemdah.core.quest.Task
import ink.ptms.chemdah.core.quest.Template
import ink.ptms.chemdah.core.quest.addon.AddonUI.Companion.ui
import ink.ptms.chemdah.core.quest.meta.MetaName.Companion.displayName
import ink.ptms.chemdah.util.*
import ink.ptms.chemdah.util.selector.InferArea
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
class AddonTrack(config: ConfigurationSection, questContainer: QuestContainer) : Addon(config, questContainer) {

    class ScoreboardContent(val content: List<String>) {

        val isQuestFormat = content.size > 1
        val value = content[0]
    }

    val center = InferArea.Single(config.getString("center").toString()).positions[0]
        get() = field.clone()

    val message = config.get("message", conf.get("default-track.message"))?.asList()?.colored() ?: emptyList()

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
    val scoreboardContent = config.getList("scoreboard-content", conf.getList("default-track.scoreboard.content"))?.run {
        filterNotNull().map {
            ScoreboardContent(it.asList())
        }
    } ?: emptyList()

    @TListener
    companion object : Listener {

        @PlayerContainer
        private val trackNavigationHologramMap = ConcurrentHashMap<String, MutableMap<String, HologramAPI.Hologram<*>>>()

        @PlayerContainer
        private val scoreboardBaffle = Baffle.of(100)

        private val playerBaffle = ConcurrentHashMap<String, MutableMap<String, Baffle>>()

        /**
         * 任务允许被追踪
         */
        fun QuestContainer.allowTracked() = track() != null

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
                if (value != null && value.allowTracked()) {
                    warning("Quest(${value.path}) not allowed to tracked.")
                    return
                }
                // 唤起事件供外部调用
                PlayerEvent.Track(player, this, value).call().nonCancelled {
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
            Bukkit.getOnlinePlayers().forEach { player ->
                val chemdahProfile = player.chemdahProfile
                val trackQuest = chemdahProfile.trackQuest ?: return
                val trackAddon = trackQuest.track() ?: return
                // 记分板刷新周期
                if (trackAddon.scoreboard && scoreboardBaffle.hasNext(player.name)) {
                    player.refreshTrackingScoreboard()
                }
                // 任务未接受
                if (chemdahProfile.getQuestById(trackQuest.id) == null) {
                    player.trackTickMark(trackAddon)
                    player.trackTickNavigation(trackAddon)
                } else {
                    trackQuest.task.forEach sub@{ (_, task) ->
                        val taskTrack = task.track() ?: return@sub
                        // 条目尚未完成
                        if (!task.isCompleted(chemdahProfile)) {
                            player.trackTickMark(taskTrack)
                            player.trackTickNavigation(taskTrack)
                        }
                    }
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
            val center = trackAddon.center
            if (center.world.name == world.name) {
                val distance = center.distance(location)
                if (distance > trackAddon.markDistanceMin) {
                    if (trackAddon.mark && trackAddon.markPeriod.hasNext(name)) {
                        signatureBaffle("${trackAddon.questContainer.path}.mark", trackAddon.markPeriod)
                        val direction = location.toVector().subtract(center.toVector()).normalize()
                        val pos = direction.multiply(distance.coerceAtMost(trackAddon.markDistanceMax))
                        Effects.create(trackAddon.markType, pos.toLocation(center.world))
                            .offset(doubleArrayOf(trackAddon.markSize, 256.0, trackAddon.markSize))
                            .count(trackAddon.markCount)
                            .player(this)
                            .play()
                    }
                }
            }
        }

        private fun Player.trackTickNavigation(trackAddon: AddonTrack) {
            val center = trackAddon.center
            if (center.world.name == world.name) {
                val distance = center.distance(location)
                if (distance < trackAddon.navigationDistanceMax) {
                    if (trackAddon.navigation && trackAddon.navigationPeriod.hasNext(name)) {
                        signatureBaffle("${trackAddon.questContainer.path}.navigation", trackAddon.navigationPeriod)
                        val pathFinder = Navigation.create(NodeEntity(location, 2.0, 1.0, canOpenDoors = true, canPassDoors = true))
                        val path = pathFinder.findPath(center, distance = trackAddon.navigationDistanceMax.toFloat())
                        path?.nodes?.forEachIndexed { index, node ->
                            Tasks.delay(index * 2L) {
                                Effects.create(trackAddon.navigationType, node.asBlockPos().toLocation(center.world).toCenterLocation())
                                    .offset(doubleArrayOf(trackAddon.navigationSizeX, trackAddon.navigationSizeY, trackAddon.navigationSizeX))
                                    .count(trackAddon.navigationCount)
                                    .player(this)
                                    .play()
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
            val chemdahProfile = chemdahProfile
            val trackQuest = chemdahProfile.trackQuest ?: return
            val trackAddon = trackQuest.track() ?: return
            val quest = chemdahProfile.getQuestById(trackQuest.id)
            // 未接受任务
            if (quest == null) {
                refreshTrackingNavigation(trackQuest, trackAddon, trackQuest.path, true)
            } else {
                trackQuest.task.forEach { (_, task) ->
                    refreshTrackingNavigation(trackQuest, task.track() ?: return@forEach, task.path, !task.isCompleted(chemdahProfile))
                }
            }
        }

        private fun Player.refreshTrackingNavigation(trackQuest: Template, trackAddon: AddonTrack, id: String, allow: Boolean) {
            val hologramMap = trackNavigationHologramMap.computeIfAbsent(name) { ConcurrentHashMap() }
            val center = trackAddon.center
            // 启用 Navigation 并在相同世界
            if (trackAddon.navigation && center.world.name == world.name && allow) {
                val distance = center.distance(location)
                val pos = location.toVector().subtract(center.toVector()).normalize().multiply(trackAddon.navigationDistanceMin)
                if (hologramMap.containsKey(id)) {
                    hologramMap[id]!!.also { holo ->
                        holo.teleport(pos.toLocation(center.world))
                        holo.edit(trackAddon.navigationContent.map {
                            it.replace("{name}", trackQuest.displayName()).replace("{distance}", Coerce.format(distance).toString())
                        })
                    }
                } else {
                    hologramMap[id] = createHologram(pos.toLocation(center.world), trackAddon.navigationContent.map {
                        it.replace("{name}", trackQuest.displayName()).replace("{distance}", Coerce.format(distance).toString())
                    })
                }
            } else {
                hologramMap.remove(id)?.delete()
            }
        }

        /**
         * 删除任务追踪
         */
        fun Player.cancelTrackingScoreboard() {
            val trackQuest = chemdahProfile.trackQuest ?: return
            val trackAddon = trackQuest.track() ?: return
            // 启用 Scoreboard
            if (trackAddon.scoreboard) {
                sendScoreboard(null)
            }
        }

        /**
         * 创建或刷新任务追踪（Scoreboard）
         */
        fun Player.refreshTrackingScoreboard() {
            val trackQuest = chemdahProfile.trackQuest ?: return
            val trackAddon = trackQuest.track() ?: return
            // 启用 Scoreboard
            if (trackAddon.scoreboard) {
                val quest = chemdahProfile.getQuestById(trackQuest.id)
                // 尚未接受任务，显示任务总信息
                val content = if (quest == null) {
                    trackAddon.scoreboardContent.flatMap {
                        if (it.isQuestFormat) {
                            it.content.flatMap { c ->
                                if (c.contains("{description}")) {
                                    trackAddon.description ?: trackQuest.ui()?.description ?: emptyList()
                                } else {
                                    c.replace("{name}", trackAddon.name ?: trackQuest.displayName()).asList()
                                }
                            }
                        } else {
                            it.content
                        }
                    }
                } else {
                    trackAddon.scoreboardContent.flatMap {
                        if (it.isQuestFormat) {
                            trackQuest.task.flatMap { (_, task) ->
                                val taskTrack = task.track()
                                if (taskTrack != null) {
                                    it.content.flatMap { c ->
                                        if (c.contains("{description}")) {
                                            taskTrack.description ?: trackQuest.ui()?.description ?: emptyList()
                                        } else {
                                            c.replace("{name}", taskTrack.name ?: trackQuest.displayName()).asList()
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
                    sendScoreboard(content[0], *content.toMutableList().run {
                        removeFirst()
                        toTypedArray()
                    })
                } else {
                    cancelTrackingScoreboard()
                }
            }
        }

        /**
         * 条目完成时刷新任务追踪
         * 已完成的条目不再显示于记分板中
         */
        @EventHandler
        private fun onComplete(e: ObjectiveEvent.Complete) {
            if (e.playerProfile.trackQuest == e.task.template) {
                e.playerProfile.player.refreshTrackingNavigation()
                e.playerProfile.player.refreshTrackingScoreboard()
            }
        }

        /**
         * 任务注册时刷新任务追踪
         * 总内容替换为子内容显示
         */
        @EventHandler
        private fun onRegistered(e: QuestEvent.Registered) {
            if (e.playerProfile.trackQuest == e.quest.template) {
                e.playerProfile.player.refreshTrackingNavigation()
                e.playerProfile.player.refreshTrackingScoreboard()
            }
        }

        /**
         * 任务注销时取消任务追踪
         */
        @EventHandler
        private fun onUnregistered(e: QuestEvent.Unregistered) {
            if (e.playerProfile.trackQuest == e.quest.template) {
                e.playerProfile.player.cancelTrackingNavigation()
                e.playerProfile.player.cancelTrackingScoreboard()
            }
        }

        /**
         * 切换追踪状态时刷新或取消任务追踪
         * 并给予相关提示
         */
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        private fun onTrack(e: PlayerEvent.Track) {
            if (e.trackingQuest != null) {
                e.player.cancelTrackingNavigation()
                e.player.cancelTrackingScoreboard()
                e.player.refreshTrackingNavigation()
                e.player.refreshTrackingScoreboard()
                e.trackingQuest.track()!!.message.forEach { message ->
                    TellrawJson.create().append(message.replace("{name}", e.trackingQuest.displayName()))
                        .hoverText(message.replace("{name}", e.trackingQuest.displayName()))
                        .clickCommand("/ChemdahTrackCancel")
                        .send(e.player)
                }
            } else {
                e.player.cancelTrackingNavigation()
                e.player.cancelTrackingScoreboard()
                TLocale.sendTo(e.player, "track-cancel")
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
        }
    }
}