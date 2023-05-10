package ink.ptms.chemdah.core.quest.addon.data

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import ink.ptms.adyeshach.api.AdyeshachAPI
import ink.ptms.chemdah.core.quest.selector.InferArea
import ink.ptms.chemdah.util.Effects
import ink.ptms.chemdah.util.asListOrLines
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.Player
import taboolib.common.platform.ProxyParticle
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.platform.function.submit
import taboolib.common.util.Vector
import taboolib.common.util.asList
import taboolib.common5.Baffle
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.chat.colored
import taboolib.module.navigation.NodeEntity
import taboolib.module.navigation.createPathfinder
import taboolib.module.nms.NMSParticle.Companion.createPacket
import taboolib.module.nms.sendBundlePacket
import taboolib.platform.util.toBukkitLocation
import taboolib.platform.util.toProxyLocation
import java.util.concurrent.TimeUnit

/**
 * 空坐标单例
 */
object NullLocation : Location(null, 0.0, 0.0, 0.0)

/**
 * 追踪中心接口
 */
interface TrackCenter {

    fun identifier(): String

    fun getLocation(player: Player): Location?
}

/**
 * 空追踪中心单例
 */
object NullTrackCenter : TrackCenter {

    override fun identifier() = "null"

    override fun getLocation(player: Player) = null
}

/**
 * Location 追踪中心
 */
class LocationTrackCenter(val center: String) : TrackCenter {

    override fun identifier(): String = center

    override fun getLocation(player: Player): Location {
        return InferArea.Single(center, false).positions[0].clone()
    }
}

/**
 * Adyeshach 追踪中心
 */
class AdyeshachTrackCenter(val id: String) : TrackCenter {

    // 坐标缓存
    val cache: Cache<String, Location> = CacheBuilder.newBuilder().expireAfterWrite(250, TimeUnit.MILLISECONDS).build()

    override fun identifier(): String = id

    override fun getLocation(player: Player): Location? {
        // 获取缓存坐标
        val loc = cache.get(player.name) { AdyeshachAPI.getEntityFromId(id, player)?.getLocation()?.add(0.0, 1.0, 0.0) ?: NullLocation }
        return if (loc is NullLocation) null else loc
    }
}

/**
 * 信标追踪配置
 */
class TrackBeacon(val config: ConfigurationSection, val root: ConfigurationSection) {

    /**
     * 是否启用
     */
    val enable = config.getBoolean("beacon", root.getBoolean("value"))

    /**
     * 粒子类型
     */
    val type = try {
        ProxyParticle.valueOf(config.getString("beacon-option.type", root.getString("type"))!!.uppercase())
    } catch (ex: Throwable) {
        ProxyParticle.VILLAGER_HAPPY
    }

    /**
     * 粒子宽度
     */
    val size = config.getDouble("beacon-option.size", root.getDouble("size"))

    /**
     * 粒子总数量
     */
    val count = config.getInt("beacon-option.count", root.getInt("count"))

    /**
     * 显示距离
     */
    val distance = config.getDouble("beacon-option.distance", root.getDouble("distance"))

    /**
     * 刷新周期
     */
    val period = Baffle.of(config.getInt("beacon-option.period", root.getInt("period")))

    /**
     * 固定位置
     */
    val fixed = config.getBoolean("beacon-option.fixed", root.getBoolean("fixed"))

    /**
     * 播放粒子
     */
    fun display(player: Player, center: Location) {
        val pos = if (fixed) {
            center
        } else {
            val direction = center.toVector().subtract(player.location.toVector()).normalize()
            player.location.add(direction.multiply(distance.coerceAtMost(distance)))
        }
        type.sendTo(adaptPlayer(player), pos.toProxyLocation(), Vector(size, 128.0, size), count)
    }
}

/**
 * 地标追踪配置
 */
class TrackLandmark(val config: ConfigurationSection, val root: ConfigurationSection) {

    /**
     * 是否启用
     */
    val enable = config.getBoolean("landmark", root.getBoolean("value"))

    /**
     * 显示内容
     */
    val content = if (config.contains("landmark-option.content")) {
        // 适配 Chemdah Lab
        config["landmark-option.content"]!!.asList().flatMap { it.lines() }.colored().ifEmpty { root["content"]!!.asList().colored() }
    } else {
        root["content"]!!.asList().colored()
    }

    /**
     * 显示距离
     */
    val distance = config.getDouble("landmark-option.distance", root.getDouble("distance"))
}

/**
 * 导航追踪配置
 */
class TrackNavigation(val config: ConfigurationSection, val root: ConfigurationSection) {

    /**
     * 是否启用
     */
    val enable = config.getBoolean("navigation", root.getBoolean("value"))

    /**
     * 是否在主线程寻路
     */
    val sync = config.getBoolean("navigation-option.sync", root.getBoolean("sync"))

    /**
     * 显示类型
     * POINT 或 ARROW
     */
    val type = config.getString("navigation-option.type", root.getString("type")).toString().uppercase()

    /**
     * 最大显示距离
     */
    val distance = config.getDouble("navigation-option.distance", root.getDouble("distance")).toFloat()

    /**
     * 点形特效相关设置
     */
    val pointType = try {
        ProxyParticle.valueOf(config.getString("navigation-option.point.type", root.getString("point.type"))!!.uppercase())
    } catch (ex: Throwable) {
        ProxyParticle.CRIT
    }
    val pointY = config.getDouble("navigation-option.point.y", root.getDouble("point.y"))
    val pointSizeX = config.getDouble("navigation-option.point.size.x", root.getDouble("point.size.x"))
    val pointSizeY = config.getDouble("navigation-option.point.size.y", root.getDouble("point.size.y"))
    val pointCount = config.getInt("navigation-option.point.count", root.getInt("point.count"))
    val pointSpeed = config.getLong("navigation-option.point.speed", root.getLong("point.speed"))
    val pointPeriod = Baffle.of(config.getInt("navigation-option.point.period", root.getInt("point.period")))

    /**
     * 箭形特效相关设置
     */
    val arrowType = try {
        Particle.valueOf(config.getString("navigation-option.arrow.type", root.getString("arrow.type"))!!.uppercase())
    } catch (ex: Throwable) {
        Particle.DRIP_LAVA
    }
    val arrowY = config.getDouble("navigation-option.arrow.y", root.getDouble("arrow.y"))
    val arrowDensity = config.getInt("navigation-option.arrow.density", root.getInt("arrow.density"))
    val arrowLen = config.getDouble("navigation-option.arrow.len", root.getDouble("arrow.len"))
    val arrowAngle = config.getDouble("navigation-option.arrow.angle", root.getDouble("arrow.angle"))
    val arrowSpeed = config.getLong("navigation-option.arrow.speed", root.getLong("arrow.speed"))
    val arrowPeriod = Baffle.of(config.getInt("navigation-option.arrow.period", root.getInt("arrow.period")))

    /**
     * 播放点型粒子效果
     */
    fun displayPoint(player: Player, center: Location) {
        submit(async = !sync) {
            // 创建寻路任务
            val pathFinder = createPathfinder(NodeEntity(player.location, 2.0, 1.0, canOpenDoors = true, canPassDoors = true))
            val path = pathFinder.findPath(center, distance)
            val nodes = path?.nodes ?: return@submit
            // 播放特效
            nodes.forEachIndexed { index, node ->
                // 速度
                submit(delay = index * pointSpeed) {
                    pointType.sendTo(
                        player = adaptPlayer(player),
                        location = node.asBlockPos().toLocation(center.world!!).add(0.5, pointY, 0.5).toProxyLocation(),
                        offset = Vector(pointSizeX, pointSizeY, pointSizeX),
                        count = pointCount
                    )
                }
            }
        }
    }

    /**
     * 播放箭型粒子效果
     */
    fun displayArrow(player: Player, center: Location) {
        submit(async = !sync) {
            val pathFinder = createPathfinder(NodeEntity(player.location, 2.0, 1.0, canOpenDoors = true, canPassDoors = true))
            val path = pathFinder.findPath(center, distance)
            val nodes = path?.nodes ?: return@submit
            // 播放特效
            (0 until (nodes.size - 1) step 2).forEach {
                // 速度
                submit(delay = it * arrowSpeed) {
                    // 起始坐标
                    val start = nodes[it].asBlockPos().toLocation(center.world!!).add(0.5, arrowY, 0.5).toProxyLocation()
                    // 结束坐标
                    val target = nodes[it + 1].asBlockPos().toLocation(center.world!!).add(0.5, arrowY, 0.5).toProxyLocation()
                    // 绘制特效
                    val packets = Effects.drawArrow(start, target, arrowDensity, arrowLen, arrowAngle).map { pos ->
                        arrowType.createPacket(pos.toBukkitLocation(), org.bukkit.util.Vector(0, 0, 0))
                    }
                    player.sendBundlePacket(packets)
                }
            }
        }
    }
}

/**
 * 记分板追踪配置
 */
class TrackScoreboard(val config: ConfigurationSection, val root: ConfigurationSection) {

    /** 记分板行信息 */
    class Line(val content: List<String>) {

        /** 是否用于显示任务信息 */
        val isQuestLine = content.size > 1
    }

    /**
     * 是否启用
     */
    val enable = config.getBoolean("scoreboard", root.getBoolean("value"))

    /**
     * 记分板长度
     */
    val length = config.getInt("scoreboard-option.length", root.getInt("length"))

    /**
     * 记分板内容
     */
    val content = if (config.getList("scoreboard-option.content")?.isNotEmpty() == true) {
        // 适配 Chemdah Lab
        config.getList("scoreboard-option.content")!!.filterNotNull().map { Line(it.asListOrLines().colored()) }
    } else {
        root.getList("content")!!.filterNotNull().map { Line(it.asList().colored()) }
    }
}