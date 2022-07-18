package ink.ptms.chemdah.core.quest.addon

import ink.ptms.chemdah.api.ChemdahAPI.chemdahProfile
import ink.ptms.chemdah.api.event.collect.ObjectiveEvents
import ink.ptms.chemdah.api.event.collect.PlayerEvents
import ink.ptms.chemdah.api.event.collect.QuestEvents
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.*
import ink.ptms.chemdah.core.quest.meta.MetaName.Companion.displayName
import ink.ptms.chemdah.core.quest.objective.Progress
import ink.ptms.chemdah.module.party.PartySystem.getMembers
import ink.ptms.chemdah.util.*
import org.bukkit.Bukkit
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarFlag
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import taboolib.common.platform.Schedule
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.platform.function.warning
import taboolib.common.util.asList
import taboolib.common5.Coerce
import taboolib.common5.mirrorFuture
import taboolib.library.configuration.ConfigurationSection
import taboolib.library.kether.QuestContext
import taboolib.module.configuration.util.getStringColored
import taboolib.module.kether.KetherShell
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.addon.AddonStats
 *
 * @author sky
 * @since 2021/3/5 11:14 上午
 */
@Id("stats")
@Option(Option.Type.SECTION)
class AddonStats(config: ConfigurationSection, questContainer: QuestContainer) : Addon(config, questContainer) {

    class StatsMap {

        val bossBar = ConcurrentHashMap<String, Couple<BossBar, Long>>()
        val bossBarAlways = ConcurrentHashMap<String, BossBar>()
    }

    /**
     * 注入接口
     */
    val agent = config["$", "pass"]!!.asList()

    /**
     * 是否可见（一段时间后隐藏）
     */
    val visible = config["visible"] == "true"

    /**
     * 是否持续可见
     */
    val visibleAlways = config["visible"] == "always"

    /**
     * BOSS 音效
     */
    val bossMusic = config.getBoolean("boss-music")

    /**
     * 黑天
     */
    val darkenSky = config.getBoolean("darken-sky")

    /**
     * 任务进度持续显示时间
     */
    val stay = config.getInt("stay", conf.getInt("default-stats.stay"))

    /**
     * 任务进度样式
     */
    val style = try {
        BarStyle.valueOf(config.getString("style", conf.getString("default-stats.style"))!!.uppercase())
    } catch (ignored: Throwable) {
        BarStyle.SOLID
    }

    /**
     * 任务进度颜色
     */
    val color = try {
        BarColor.valueOf(config.getString("color", conf.getString("default-stats.color"))!!.uppercase())
    } catch (ignored: Throwable) {
        BarColor.WHITE
    }

    /**
     * 进度原始内容
     */
    val content = config.getString("content", conf.getStringColored("default-stats.content")).toString()

    /**
     * 进度格式化后的内容
     */
    fun getTitle(task: QuestContainer, progress: Progress) = content.replaces(
        "name" to task.displayName(),
        "value" to progress.value,
        "target" to progress.target,
        "percent" to Coerce.format(progress.percent * 100)
    )

    /**
     * 获取进度
     * 获取某个单独的条目进度，也可以获取整个任务中的所有条目进度总和
     * 但是所有条目的进度必须是数字才可以被正常叠加
     */
    fun getProgress(profile: PlayerProfile, task: Task? = null): CompletableFuture<Progress> {
        if (task != null) {
            val future = CompletableFuture<Progress>()
            val quest = profile.getQuests(openAPI = true).firstOrNull { it.id == task.template.id }
            if (quest == null) {
                warning("Quest(${questContainer.node}) not accepted.")
                future.complete(Progress.ZERO)
                return future
            }
            val vars = AtomicReference<QuestContext.VarTable>()
            KetherShell.eval(agent, sender = adaptPlayer(profile.player), namespace = namespaceQuest) {
                vars.set(rootFrame().variables().also { vars ->
                    vars.set("@QuestContainer", task)
                })
            }.thenApply {
                task.objective.getProgress(profile, task).run {
                    future.complete(
                        Progress(
                            vars.get().get<Any?>("value").orElse(value),
                            vars.get().get<Any?>("target").orElse(target),
                            vars.get().get<Any?>("percent").orElse(null).asDouble(percent)
                        )
                    )
                }
            }
            return future
        }
        return if (questContainer is Template) {
            val future = CompletableFuture<Progress>()
            val tasks = questContainer.taskMap.values.toList()
            var p = Progress.ZERO
            fun process(cur: Int) {
                if (cur < tasks.size) {
                    getProgress(profile, tasks[cur]).thenAccept {
                        p = Progress(p.value.increaseAny(it.value), p.target.increaseAny(it.target), p.percent + (it.percent / tasks.size))
                        process(cur + 1)
                    }
                } else {
                    future.complete(p)
                }
            }
            process(0)
            future
        } else {
            getProgress(profile, questContainer as Task)
        }
    }

    companion object {

        private val statsMap = ConcurrentHashMap<String, StatsMap>()

        @Schedule(period = 20)
        internal fun bossBarRemove20() {
            statsMap.forEach { (_, statsMap) ->
                statsMap.bossBar.forEach {
                    if (it.value.value < System.currentTimeMillis()) {
                        it.value.key.removeAll()
                        statsMap.bossBar.remove(it.key)
                    }
                }
            }
        }

        @SubscribeEvent
        internal fun onReleased(e: PlayerEvents.Released) {
            statsMap.remove(e.player.name)
        }

        @SubscribeEvent
        internal fun onSelected(e: PlayerEvents.Selected) {
            e.playerProfile.getQuests().forEach { quest ->
                quest.getMembers(self = true).forEach {
                    quest.refreshStatusAlwaysType(it.chemdahProfile)
                }
            }
        }

        @SubscribeEvent
        internal fun onRegistered(e: QuestEvents.Registered) {
            e.quest.getMembers(self = true).forEach {
                e.quest.refreshStatusAlwaysType(it.chemdahProfile)
            }
        }

        @SubscribeEvent
        internal fun onUnregistered(e: QuestEvents.Unregistered) {
            e.quest.getMembers(self = true).forEach {
                e.quest.hiddenStats(it.chemdahProfile)
            }
        }

        @SubscribeEvent
        internal fun onCompletePost(e: ObjectiveEvents.Complete.Post) {
            e.quest.getMembers(self = true).forEach {
                e.task.hiddenStats(it.chemdahProfile)
            }
        }

        @SubscribeEvent
        internal fun onContinuePost(e: ObjectiveEvents.Continue.Post) {
            e.quest.getMembers(self = true).forEach {
                e.task.refreshStats(it.chemdahProfile)
                e.quest.template.refreshStats(it.chemdahProfile)
            }
        }

        fun QuestContainer.stats() = addon<AddonStats>("stats")

        /**
         * 通过可能存在的 Stats 扩展展示条目进度
         */
        fun QuestContainer.statsDisplay(profile: PlayerProfile): CompletableFuture<BossBar?> {
            val future = CompletableFuture<BossBar?>()
            val stats = stats()
            if (stats == null) {
                future.complete(null)
                return future
            }
            mirrorFuture<Int>("AddonStats:statsDisplay") {
                getProgress(profile).thenApply { progress ->
                    val bossBar = Bukkit.createBossBar("", stats.color, stats.style)
                    if (stats.darkenSky) {
                        bossBar.addFlag(BarFlag.DARKEN_SKY)
                    }
                    if (stats.bossMusic) {
                        bossBar.addFlag(BarFlag.PLAY_BOSS_MUSIC)
                    }
                    bossBar.progress = progress.percent
                    bossBar.setTitle(stats.getTitle(this@statsDisplay, progress))
                    bossBar.addPlayer(profile.player)
                    future.complete(bossBar)
                    finish(0)
                }
            }
            return future
        }

        /**
         * 获取条目进度
         * 并通过可能存在的 Stats 扩展
         */
        fun QuestContainer.getProgress(profile: PlayerProfile): CompletableFuture<Progress> {
            val progress = stats()?.getProgress(profile)
            if (progress != null) {
                return progress
            }
            return when (this) {
                is Template -> {
                    var p = Progress.ZERO
                    taskMap.forEach { (_, task) ->
                        val tp = task.objective.getProgress(profile, task)
                        p = Progress(p.value.increaseAny(tp.value), p.target.increaseAny(tp.target), p.percent + (tp.percent / this.taskMap.size))
                    }
                    CompletableFuture.completedFuture(p)
                }
                is Task -> {
                    CompletableFuture.completedFuture(objective.getProgress(profile, this))
                }
                else -> error("out of case")
            }
        }

        /**
         * 隐藏任务进度
         */
        fun Quest.hiddenStats(profile: PlayerProfile) {
            val statsMap = statsMap.computeIfAbsent(profile.player.name) { StatsMap() }
            // 任务
            statsMap.bossBar.remove(template.path)?.key?.removeAll()
            statsMap.bossBarAlways.remove(template.path)?.removeAll()
            // 条目
            tasks.forEach {
                statsMap.bossBar.remove(it.path)?.key?.removeAll()
                statsMap.bossBarAlways.remove(it.path)?.removeAll()
            }
        }

        /**
         * 隐藏条目进度
         */
        fun Task.hiddenStats(profile: PlayerProfile) {
            val statsMap = statsMap.computeIfAbsent(profile.player.name) { StatsMap() }
            statsMap.bossBar.remove(path)?.key?.removeAll()
            statsMap.bossBarAlways.remove(path)?.removeAll()
        }

        /**
         * 刷新任务进度（仅持续显示的）
         */
        fun Quest.refreshStatusAlwaysType(profile: PlayerProfile) {
            val statsMap = statsMap.computeIfAbsent(profile.player.name) { StatsMap() }
            // 任务
            if (template.stats()?.visibleAlways == true) {
                template.statsDisplay(profile).thenApply { bossBar ->
                    if (bossBar != null) {
                        statsMap.bossBarAlways.put(template.path, bossBar)?.removeAll()
                    }
                }
            }
            // 条目
            tasks.forEach {
                if (it.stats()?.visibleAlways == true) {
                    it.statsDisplay(profile).thenApply { bossBar ->
                        if (bossBar != null) {
                            statsMap.bossBarAlways.put(it.path, bossBar)?.removeAll()
                        }
                    }
                }
            }
        }

        /**
         * 刷新条目进度
         */
        fun QuestContainer.refreshStats(profile: PlayerProfile) {
            val stats = stats() ?: return
            val statsMap = statsMap.computeIfAbsent(profile.player.name) { StatsMap() }
            mirrorFuture<Int>("AddonStats:onContinue") {
                when {
                    stats.visibleAlways -> {
                        val bossBar = statsMap.bossBarAlways[path]
                        if (bossBar == null) {
                            statsDisplay(profile).thenApply { bar ->
                                if (bar != null) {
                                    statsMap.bossBarAlways.put(path, bar)?.removeAll()
                                }
                                finish(0)
                            }
                        } else {
                            getProgress(profile).thenApply { progress ->
                                bossBar.progress = progress.percent
                                bossBar.setTitle(stats.getTitle(this@refreshStats, progress))
                                finish(0)
                            }
                        }
                    }
                    stats.visible -> {
                        val bossBar = statsMap.bossBar[path]
                        if (bossBar == null) {
                            statsDisplay(profile).thenApply { bar ->
                                if (bar != null) {
                                    statsMap.bossBar.put(path, Couple(bar, System.currentTimeMillis() + (stats.stay * 50L)))?.key?.removeAll()
                                }
                                finish(0)
                            }
                        } else {
                            getProgress(profile).thenApply { progress ->
                                bossBar.key.progress = progress.percent
                                bossBar.key.setTitle(stats.getTitle(this@refreshStats, progress))
                                bossBar.value = System.currentTimeMillis() + (stats.stay * 50L)
                                finish(0)
                            }
                        }
                    }
                }
            }
        }
    }
}