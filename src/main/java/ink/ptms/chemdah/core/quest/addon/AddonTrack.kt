package ink.ptms.chemdah.core.quest.addon

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.api.ChemdahAPI.chemdahProfile
import ink.ptms.chemdah.api.event.PlayerEvent
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Id
import ink.ptms.chemdah.core.quest.QuestContainer
import ink.ptms.chemdah.core.quest.Template
import ink.ptms.chemdah.core.quest.meta.MetaName.Companion.displayName
import ink.ptms.chemdah.util.InferArea
import ink.ptms.chemdah.util.asList
import ink.ptms.chemdah.util.colored
import ink.ptms.chemdah.util.conf
import io.izzel.taboolib.module.inject.TSchedule
import io.izzel.taboolib.module.tellraw.TellrawJson
import io.izzel.taboolib.util.Baffle
import io.izzel.taboolib.util.lite.Effects
import org.bukkit.Bukkit
import org.bukkit.Particle
import org.bukkit.configuration.ConfigurationSection

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
    val navigationDistance = config.getDouble("navigation-option.distance", conf.getDouble("default-track.navigation.distance"))
    val navigationPeriod = Baffle.of(config.getInt("navigation-option.period", conf.getInt("default-track.navigation.period")))

    val scoreboard = config.getBoolean("scoreboard", conf.getBoolean("default-track.scoreboard.value"))
    val scoreboardContent = config.getList("scoreboard-content", conf.getList("default-track.scoreboard.content"))?.run {
        filterNotNull().map {
            ScoreboardContent(it.asList())
        }
    } ?: emptyList()

    companion object {

        /**
         * 任务追踪扩展
         */
        fun Template.track() = addon<AddonTrack>("track")

        /**
         * 任务允许被追踪
         */
        fun Template.isTrackable() = track() != null

        /**
         * 当前任务追踪
         */
        var PlayerProfile.trackQuest: Template?
            set(value) {
                if (value != null && value.isTrackable()) {
                    PlayerEvent.Track(player, this, value).call().nonCancelled {
                        persistentDataContainer["quest.track"] = value.id
                        // 发送信息
                        value.track()!!.message.forEach { message ->
                            TellrawJson.create().append(message.replace("{name}", value.displayName()))
                                .hoverText(message.replace("{name}", value.displayName()))
                                .clickCommand("/ChemdahTrackCancel")
                                .send(player)
                        }
                    }
                } else {
                    persistentDataContainer.remove("quest.track")
                }
            }
            get() = persistentDataContainer["quest.track"]?.let { ChemdahAPI.getQuestTemplate(it.toString()) }

        /**
         * 地标及导航追踪
         */
        @TSchedule(period = 1, async = true)
        fun trackMark() {
            Bukkit.getOnlinePlayers().forEach { player ->
                val trackQuest = player.chemdahProfile.trackQuest ?: return
                val trackAddon = trackQuest.track() ?: return
                // 地标渲染周期
                if (trackAddon.mark && trackAddon.markPeriod.hasNext()) {
                    val center = trackAddon.center
                    if (center.world.name == player.world.name) {
                        // 计算距离
                        val distance = center.distance(player.location)
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
                }
                // 导航渲染周期
                if (trackAddon.navigation && trackAddon.navigationPeriod.hasNext()) {

                }
            }
        }
    }
}