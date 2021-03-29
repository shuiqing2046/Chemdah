package ink.ptms.chemdah.core.quest.addon

import ink.ptms.chemdah.api.event.ObjectiveEvent
import ink.ptms.chemdah.api.event.QuestEvent
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Id
import ink.ptms.chemdah.core.quest.QuestContainer
import ink.ptms.chemdah.core.quest.Task
import ink.ptms.chemdah.core.quest.addon.AddonStats.Companion.statsDisplay
import ink.ptms.chemdah.core.quest.meta.MetaName.Companion.displayName
import ink.ptms.chemdah.core.quest.objective.Progress
import ink.ptms.chemdah.util.*
import io.izzel.taboolib.kotlin.kether.KetherShell
import io.izzel.taboolib.kotlin.kether.common.api.QuestContext
import io.izzel.taboolib.module.inject.PlayerContainer
import io.izzel.taboolib.module.inject.TListener
import io.izzel.taboolib.module.inject.TSchedule
import io.izzel.taboolib.util.Coerce
import io.izzel.taboolib.util.Pair
import org.bukkit.Bukkit
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarFlag
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.addon.AddonProgress
 *
 * @author sky
 * @since 2021/3/5 11:14 上午
 */
@Id("stats")
class AddonStats(config: ConfigurationSection, questContainer: QuestContainer) : Addon(config, questContainer) {

    class StatsMap {

        val bossBar = ConcurrentHashMap<String, Pair<BossBar, Long>>()
        val bossBarAlways = ConcurrentHashMap<String, BossBar>()
    }

    val agent = config.get("$", "pass")!!.asList()
    val visible = config.getBoolean("visible")
    val visibleAlways = config.getBoolean("visible-always")
    val bossMusic = config.getBoolean("boss-music")
    val darkenSky = config.getBoolean("darken-sky")
    val stay = config.getInt("stay", conf.getInt("default-stats.stay"))

    val style = try {
        BarStyle.valueOf(config.getString("style", conf.getString("default-stats.style"))!!.toUpperCase())
    } catch (ignored: Throwable) {
        BarStyle.SOLID
    }

    val color = try {
        BarColor.valueOf(config.getString("color", conf.getString("default-stats.color"))!!.toUpperCase())
    } catch (ignored: Throwable) {
        BarColor.WHITE
    }

    val content = config.getString("content", conf.getStringColored("default-stats.content")).toString()

    fun getTitle(task: Task, progress: Progress) = content
        .replace("{name}", task.displayName())
        .replace("{value}", progress.value.toString())
        .replace("{target}", progress.target.toString())
        .replace("{percent}", Coerce.format(progress.percent * 100).toString())

    fun getProgress(profile: PlayerProfile): CompletableFuture<Progress> {
        val future = CompletableFuture<Progress>()
        val task = questContainer as? Task
        if (task == null) {
            warning("Template(${questContainer.path}) not support addon(Stats).")
            future.complete(Progress.empty)
            return future
        }
        val quest = profile.quests.firstOrNull { it.id == task.template.id }
        if (quest == null) {
            warning("Quest(${questContainer.node}) not accepted.")
            future.complete(Progress.empty)
            return future
        }
        val vars = AtomicReference<QuestContext.VarTable>()
        KetherShell.eval(agent, namespace = namespaceQuest) {
            sender = profile.player
            vars.set(rootFrame().variables().also { vars ->
                vars.set("@Quest", quest)
                vars.set("@QuestContainer", task)
            })
        }.thenApply {
            val op = task.objective.getProgress(profile, task)
            future.complete(
                Progress(
                    vars.get().get<Any?>("value") ?: op.value,
                    vars.get().get<Any?>("target") ?: op.target,
                    vars.get().get<Any?>("percent").asDouble(op.percent)
                )
            )
        }
        return future
    }

    @TListener
    companion object : Listener {

        @PlayerContainer
        private val statsMap = ConcurrentHashMap<String, StatsMap>()

        @TSchedule(period = 20)
        private fun e() {
            statsMap.forEach { (_, statsMap) ->
                statsMap.bossBar.forEach {
                    if (it.value.value < System.currentTimeMillis()) {
                        it.value.key.removeAll()
                        statsMap.bossBar.remove(it.key)
                    }
                }
            }
        }

        @EventHandler
        private fun e(e: QuestEvent.Registered) {
            val statsMap = statsMap.computeIfAbsent(e.playerProfile.player.name) { StatsMap() }
            e.quest.tasks.forEach {
                if (it.stats()?.visibleAlways == true) {
                    it.statsDisplay(e.playerProfile).thenApply { bossBar ->
                        if (bossBar != null) {
                            statsMap.bossBarAlways[it.path] = bossBar
                        }
                    }
                }
            }
        }

        @EventHandler
        private fun e(e: QuestEvent.Unregistered) {
            val statsMap = statsMap.computeIfAbsent(e.playerProfile.player.name) { StatsMap() }
            e.quest.tasks.forEach {
                statsMap.bossBar.remove(it.path)?.key?.removeAll()
                statsMap.bossBarAlways.remove(it.path)?.removeAll()
            }
        }

        @EventHandler
        private fun e(e: ObjectiveEvent.Continue) {
            val stats = e.task.stats() ?: return
            val statsMap = statsMap.computeIfAbsent(e.playerProfile.player.name) { StatsMap() }
            mirrorFuture("AddonStats:onContinue") {
                when {
                    stats.visible -> {
                        val bossBar = statsMap.bossBar[e.task.path]
                        if (bossBar == null) {
                            e.task.statsDisplay(e.playerProfile).thenApply { bar ->
                                if (bar != null) {
                                    statsMap.bossBar[e.task.path] = Pair.of(bar, System.currentTimeMillis() + (stats.stay * 50L))
                                }
                                finish()
                            }
                        } else {
                            e.task.getProgress(e.playerProfile).thenApply { progress ->
                                bossBar.key.progress = progress.percent
                                bossBar.key.setTitle(stats.getTitle(e.task, progress))
                                bossBar.value = System.currentTimeMillis() + (stats.stay * 50L)
                                finish()
                            }
                        }
                    }
                    stats.visibleAlways -> {
                        val bossBar = statsMap.bossBarAlways[e.task.path]
                        if (bossBar == null) {
                            e.task.statsDisplay(e.playerProfile).thenApply { bar ->
                                if (bar != null) {
                                    statsMap.bossBarAlways[e.task.path] = bar
                                }
                                finish()
                            }
                        } else {
                            e.task.getProgress(e.playerProfile).thenApply { progress ->
                                bossBar.progress = progress.percent
                                bossBar.setTitle(stats.getTitle(e.task, progress))
                                finish()
                            }
                        }
                    }
                }
            }
        }

        fun Task.stats() = addon<AddonStats>("stats")

        /**
         * 通过可能存在的 Stats 扩展展示条目进度
         */
        fun Task.statsDisplay(profile: PlayerProfile): CompletableFuture<BossBar?> {
            val future = CompletableFuture<BossBar?>()
            val stats = stats()
            if (stats == null) {
                future.complete(null)
                return future
            }
            mirrorFuture("AddonStats:statsDisplay") {
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
                    finish()
                }
            }
            return future
        }

        /**
         * 获取条目进度
         * 并通过可能存在的 Stats 扩展
         */
        fun Task.getProgress(profile: PlayerProfile): CompletableFuture<Progress> {
            return stats()?.getProgress(profile) ?: CompletableFuture.completedFuture(objective.getProgress(profile, this))
        }
    }
}