package ink.ptms.chemdah.core.quest.addon

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.api.ChemdahAPI.chemdahProfile
import ink.ptms.chemdah.api.HologramAPI
import ink.ptms.chemdah.api.HologramAPI.createHologram
import ink.ptms.chemdah.api.event.PlayerEvent
import ink.ptms.chemdah.api.event.QuestEvent
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Id
import ink.ptms.chemdah.core.quest.QuestContainer
import ink.ptms.chemdah.core.quest.Template
import ink.ptms.chemdah.core.quest.meta.MetaName.Companion.displayName
import ink.ptms.chemdah.util.*
import io.izzel.taboolib.kotlin.Tasks
import io.izzel.taboolib.kotlin.navigation.Navigation
import io.izzel.taboolib.kotlin.navigation.pathfinder.NodeEntity
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
        private val trackNavigationHologramMap = ConcurrentHashMap<String, HologramAPI.Hologram<*>>()

        /**
         * 任务追踪扩展
         */
        fun Template.track() = addon<AddonTrack>("track")

        /**
         * 任务允许被追踪
         */
        fun Template.allowTracked() = track() != null

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
                PlayerEvent.Track(player, this, value).call().nonCancelled {
                    if (value != null) {
                        persistentDataContainer["quest.track"] = value.id
                    } else {
                        persistentDataContainer.remove("quest.track")
                    }
                }
            }
            get() = persistentDataContainer["quest.track"]?.let { ChemdahAPI.getQuestTemplate(it.toString()) }

        /**
         * 地标及导航追踪
         */
        @TSchedule(period = 1, async = true)
        private fun trackMark() {
            Bukkit.getOnlinePlayers().forEach { player ->
                val trackQuest = player.chemdahProfile.trackQuest ?: return
                val trackAddon = trackQuest.track() ?: return
                val center = trackAddon.center
                // 位于相同世界
                if (center.world.name == player.world.name) {
                    // 计算距离
                    val distance = center.distance(player.location)
                    // 地标渲染周期
                    if (trackAddon.mark && trackAddon.markPeriod.hasNext()) {
                        if (distance > trackAddon.markDistanceMin) {
                            val direction = player.location.toVector().subtract(center.toVector()).normalize()
                            val pos = direction.multiply(distance.coerceAtMost(trackAddon.markDistanceMax))
                            Effects.create(trackAddon.markType, pos.toLocation(center.world))
                                .offset(doubleArrayOf(trackAddon.markSize, 256.0, trackAddon.markSize))
                                .count(trackAddon.markCount)
                                .player(player)
                                .play()
                        }
                    }
                    // 导航渲染周期
                    if (trackAddon.navigation && trackAddon.navigationPeriod.hasNext()) {
                        if (distance < trackAddon.navigationDistanceMax) {
                            val pathFinder = Navigation.create(NodeEntity(player.location, 2.0, 1.0, canOpenDoors = true, canPassDoors = true))
                            val path = pathFinder.findPath(center, distance = trackAddon.navigationDistanceMax.toFloat())
                            path?.nodes?.forEachIndexed { index, node ->
                                Tasks.delay(index * 2L) {
                                    Effects.create(trackAddon.navigationType, node.asBlockPos().toLocation(center.world).toCenterLocation())
                                        .offset(doubleArrayOf(trackAddon.navigationSizeX, trackAddon.navigationSizeY, trackAddon.navigationSizeX))
                                        .count(trackAddon.navigationCount)
                                        .player(player)
                                        .play()
                                }
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
                trackNavigationHologramMap.remove(name)!!.delete()
            }
        }

        /**
         * 创建或更新任务追踪（Navigation）
         */
        fun Player.refreshTrackingNavigation() {
            val trackQuest = chemdahProfile.trackQuest ?: return
            val trackAddon = trackQuest.track() ?: return
            val center = trackAddon.center
            // 启用 Navigation 并在相同世界
            if (trackAddon.navigation && center.world.name == world.name) {
                val distance = center.distance(location)
                val pos = location.toVector().subtract(center.toVector()).normalize().multiply(trackAddon.navigationDistanceMin)
                if (trackNavigationHologramMap.containsKey(name)) {
                    trackNavigationHologramMap[name]!!.also { holo ->
                        holo.teleport(pos.toLocation(center.world))
                        holo.edit(trackAddon.navigationContent.map {
                            it.replace("{name}", trackQuest.displayName()).replace("{distance}", Coerce.format(distance).toString())
                        })
                    }
                } else {
                    trackNavigationHologramMap[name] = createHologram(pos.toLocation(center.world), trackAddon.navigationContent.map {
                        it.replace("{name}", trackQuest.displayName()).replace("{distance}", Coerce.format(distance).toString())
                    })
                }
            } else {
                cancelTrackingNavigation()
            }
        }

        @EventHandler
        private fun e(e: QuestEvent.Unregistered) {
            if (e.playerProfile.trackQuest == e.quest.template) {
                e.playerProfile.player.cancelTrackingNavigation()
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        private fun e(e: PlayerEvent.Track) {
            if (e.trackingQuest != null) {
                e.player.cancelTrackingNavigation()
                e.player.refreshTrackingNavigation()
                e.trackingQuest.track()!!.message.forEach { message ->
                    TellrawJson.create().append(message.replace("{name}", e.trackingQuest.displayName()))
                        .hoverText(message.replace("{name}", e.trackingQuest.displayName()))
                        .clickCommand("/ChemdahTrackCancel")
                        .send(e.player)
                }
            } else {
                e.player.cancelTrackingNavigation()
                TLocale.sendTo(e.player, "track-cancel")
            }
        }

        @EventHandler
        private fun e(e: PlayerMoveEvent) {
            if (e.from.toVector() != e.to.toVector()) {
                e.player.refreshTrackingNavigation()
            }
        }

        @EventHandler
        private fun e(e: PlayerCommandPreprocessEvent) {
            if (e.message.equals("/ChemdahTrackCancel", true)) {
                e.isCancelled = true
                e.player.chemdahProfile.trackQuest = null
            }
        }
    }
}