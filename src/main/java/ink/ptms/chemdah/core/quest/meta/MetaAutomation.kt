package ink.ptms.chemdah.core.quest.meta

import com.google.common.base.Enums
import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.core.quest.*
import io.izzel.taboolib.module.inject.TSchedule
import io.izzel.taboolib.util.Coerce
import io.izzel.taboolib.util.lite.cooldown.RealTime
import io.izzel.taboolib.util.lite.cooldown.RealTimeUnit
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.meta.MetaAutomation
 *
 * @author sky
 * @since 2021/3/1 11:47 下午
 */
@Id("automation")
@MetaType(MetaType.Type.SECTION)
class MetaAutomation(source: ConfigurationSection?, questContainer: QuestContainer) : Meta<ConfigurationSection?>(source, questContainer) {

    val isAutoAccept = source?.getBoolean("auto-accept")

    val plan = if (source != null && source.contains("plan")) {
        val method = Enums.getIfPresent(RealTime::class.java, source.getString("method").toString().toUpperCase()).or(RealTime.START_IN_MONDAY)
        val args = source.getString("type").toString().toLowerCase().split(" ")
        val type = when (args[0]) {
            "hour" -> PlanTypeHour(
                method,
                RealTimeUnit.HOUR,
                Coerce.toInteger(args[1]),
            )
            "day", "daily" -> PlanTypeDaily(
                method,
                RealTimeUnit.DAY,
                Coerce.toInteger(args[1]),
                Coerce.toInteger(args.getOrNull(2) ?: 6),
                Coerce.toInteger(args.getOrNull(3) ?: 0)
            )
            "week", "weekly" -> PlanTypeWeekly(
                method,
                RealTimeUnit.WEEK,
                Coerce.toInteger(args[1]),
                Coerce.toInteger(args.getOrNull(2) ?: 0),
                Coerce.toInteger(args.getOrNull(3) ?: 6),
                Coerce.toInteger(args.getOrNull(4) ?: 0)
            )
            else -> null
        }
        if (type != null) {
            Plan(type, source.getInt("count", 1), source.getString("group"))
        } else {
            null
        }
    } else {
        null
    }

    class Plan(val type: PlanType, val count: Int, val group: String?) {

        val nextTime: Long
            get() = type.nextTime
    }

    abstract class PlanType(val realTime: RealTime, val unit: RealTimeUnit, val value: Int) {

        abstract val nextTime: Long
    }

    class PlanTypeHour(realTime: RealTime, unit: RealTimeUnit, value: Int) : PlanType(realTime, unit, value) {

        override val nextTime = Calendar.getInstance().run {
            this.timeInMillis = realTime.nextTime(RealTimeUnit.HOUR, value)
            this.timeInMillis
        }
    }

    class PlanTypeDaily(realTime: RealTime, unit: RealTimeUnit, value: Int, hour: Int, minute: Int) : PlanType(realTime, unit, value) {

        override val nextTime = Calendar.getInstance().run {
            this.timeInMillis = realTime.nextTime(RealTimeUnit.DAY, value)
            this.add(Calendar.HOUR, hour)
            this.add(Calendar.MINUTE, minute)
            this.timeInMillis
        }
    }

    class PlanTypeWeekly(realTime: RealTime, unit: RealTimeUnit, value: Int, day: Int, hour: Int, minute: Int) : PlanType(realTime, unit, value) {

        override val nextTime = Calendar.getInstance().run {
            this.timeInMillis = realTime.nextTime(RealTimeUnit.WEEK, value)
            this.add(Calendar.DAY_OF_WEEK, day)
            this.add(Calendar.HOUR, hour)
            this.add(Calendar.MINUTE, minute)
            this.timeInMillis
        }
    }

    class Group(val groupId: String, val plan: Plan) {

        val quests = ArrayList<Template>()
    }

    companion object {

        fun Template.isAutoAccept() = meta<MetaAutomation>("automation")?.isAutoAccept ?: false

        fun Template.plan() = meta<MetaAutomation>("automation")?.plan

        @TSchedule(period = 20, async = true)
        fun automation() {
            val groups = HashMap<String, Group>()
            val autoAccept = ArrayList<Template>()
            ChemdahAPI.quest.forEach { (_, quest) ->
                if (quest.isAutoAccept()) {
                    autoAccept.add(quest)
                } else {
                    val plan = quest.plan()
                    if (plan != null) {
                        val id = if (plan.group != null) "@${plan.group}" else quest.id
                        val group = groups.computeIfAbsent(id) { Group(id, plan) }
                        group.quests.add(quest)
                    }
                }
            }
            Bukkit.getOnlinePlayers().forEach { player ->
                val profile = ChemdahAPI.getPlayerProfile(player)
                // 自动接受的任务
                autoAccept.forEach {
                    if (profile.getQuests(it.id).isNotEmpty()) {
                        it.acceptTo(profile)
                    }
                }
                // 定时计划
                groups.forEach { (id, group) ->
                    val nextTime = profile.persistentDataContainer["automation.$id.next", 0L].toLong()
                    if (nextTime < System.currentTimeMillis()) {
                        profile.persistentDataContainer["automation.$id.next"] = group.plan.nextTime
                        val pool = group.quests.toMutableList()
                        var i = group.plan.count
                        fun process() {
                            if (i > 0 && pool.isNotEmpty()) {
                                pool.removeAt(Random.nextInt(pool.size)).acceptTo(profile).thenAccept {
                                    if (it == AcceptResult.SUCCESSFUL) {
                                        i--
                                    }
                                    process()
                                }
                            }
                        }
                        process()
                    }
                }
            }
        }
    }
}
