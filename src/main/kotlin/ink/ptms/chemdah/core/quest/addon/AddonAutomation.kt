package ink.ptms.chemdah.core.quest.addon

import com.google.common.base.Enums
import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.api.ChemdahAPI.chemdahProfile
import ink.ptms.chemdah.api.ChemdahAPI.isChemdahProfileLoaded
import ink.ptms.chemdah.core.quest.*
import org.bukkit.Bukkit
import taboolib.common.platform.Schedule
import taboolib.common5.Coerce
import taboolib.common5.RealTime
import taboolib.common5.mirrorFuture
import taboolib.library.configuration.ConfigurationSection
import java.util.*
import kotlin.random.Random

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.addon.AddonAutomation
 *
 * @author sky
 * @since 2021/3/1 11:47 下午
 */
@Id("automation")
@Option(Option.Type.SECTION)
class AddonAutomation(source: ConfigurationSection, questContainer: QuestContainer) : Addon(source, questContainer) {

    class Plan(val type: PlanType, val count: Int, val group: String?) {

        val nextTime: Long
            get() = type.nextTime
    }

    abstract class PlanType(val realTime: RealTime, val unit: RealTime.Type, val value: Int) {

        abstract val nextTime: Long
    }

    class PlanTypeHour(realTime: RealTime, unit: RealTime.Type, value: Int) : PlanType(realTime, unit, value) {

        override val nextTime = Calendar.getInstance().run {
            this.timeInMillis = realTime.nextTime(RealTime.Type.HOUR, value)
            this.timeInMillis
        }
    }

    class PlanTypeDaily(realTime: RealTime, unit: RealTime.Type, value: Int, hour: Int, minute: Int) : PlanType(realTime, unit, value) {

        override val nextTime = Calendar.getInstance().run {
            this.timeInMillis = realTime.nextTime(RealTime.Type.DAY, value)
            this.add(Calendar.HOUR, hour)
            this.add(Calendar.MINUTE, minute)
            this.timeInMillis
        }
    }

    class PlanTypeWeekly(realTime: RealTime, unit: RealTime.Type, value: Int, day: Int, hour: Int, minute: Int) : PlanType(realTime, unit, value) {

        override val nextTime = Calendar.getInstance().run {
            this.timeInMillis = realTime.nextTime(RealTime.Type.WEEK, value)
            this.add(Calendar.DAY_OF_WEEK, day)
            this.add(Calendar.HOUR, hour)
            this.add(Calendar.MINUTE, minute)
            this.timeInMillis
        }
    }

    class Group(val groupId: String, val plan: Plan) {

        val quests = ArrayList<Template>()
    }

    val isAutoAccept = source.getBoolean("auto-accept")

    val plan = if (source.contains("plan")) {
        val method = Enums.getIfPresent(RealTime::class.java, source.getString("plan.method").toString().toUpperCase()).or(RealTime.START_IN_MONDAY)
        val args = source.getString("plan.type").toString().toLowerCase().split(" ")
        val type = when (args[0]) {
            "hour" -> PlanTypeHour(
                method,
                RealTime.Type.HOUR,
                Coerce.toInteger(args[1]),
            )
            "day", "daily" -> PlanTypeDaily(
                method,
                RealTime.Type.DAY,
                Coerce.toInteger(args[1]),
                Coerce.toInteger(args.getOrNull(2) ?: 6),
                Coerce.toInteger(args.getOrNull(3) ?: 0)
            )
            "week", "weekly" -> PlanTypeWeekly(
                method,
                RealTime.Type.WEEK,
                Coerce.toInteger(args[1]),
                Coerce.toInteger(args.getOrNull(2) ?: 0),
                Coerce.toInteger(args.getOrNull(3) ?: 6),
                Coerce.toInteger(args.getOrNull(4) ?: 0)
            )
            else -> null
        }
        if (type != null) {
            Plan(type, source.getInt("plan.count", 1), source.getString("plan.group"))
        } else {
            null
        }
    } else {
        null
    }

    val planGroup = source.getString("plan.group")

    companion object {

        fun Template.isAutoAccept() = addon<AddonAutomation>("automation")?.isAutoAccept ?: false

        fun Template.plan() = addon<AddonAutomation>("automation")?.plan

        fun Template.planGroup() = addon<AddonAutomation>("automation")?.planGroup

        @Schedule(period = 20, async = true)
        fun automation() {
            if (Bukkit.getOnlinePlayers().isEmpty()) {
                return
            }
            val groups = HashMap<String, Group>()
            val autoAccept = ArrayList<Template>()
            // 优先加载拥有主动逻辑的 Plan 计划
            ChemdahAPI.questTemplate.forEach { (_, quest) ->
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
            // 加载没有主动逻辑的被 Plan Group 收录的任务
            ChemdahAPI.questTemplate.forEach { (_, quest) ->
                if (quest.plan() == null) {
                    val group = quest.planGroup()
                    if (group != null && groups.containsKey("@$group")) {
                        groups["@$group"]!!.quests.add(quest)
                    }
                }
            }
            if (groups.isEmpty() && autoAccept.isEmpty()) {
                return
            }
            mirrorFuture<Int>("MetaAutomation") {
                Bukkit.getOnlinePlayers().filter { it.isChemdahProfileLoaded }.forEach { player ->
                    val profile = player.chemdahProfile
                    // 自动接受的任务
                    autoAccept.forEach {
                        if (profile.getQuestById(it.id, openAPI = false) == null) {
                            it.acceptTo(profile)
                        }
                    }
                    // 定时计划
                    groups.forEach { (id, group) ->
                        val nextTime = profile.persistentDataContainer["quest.automation.$id.next", 0L].toLong()
                        if (nextTime < System.currentTimeMillis()) {
                            profile.persistentDataContainer["quest.automation.$id.next"] = group.plan.nextTime
                            val pool = group.quests.toMutableList()
                            var i = group.plan.count
                            fun process() {
                                if (i > 0 && pool.isNotEmpty()) {
                                    pool.removeAt(Random.nextInt(pool.size)).acceptTo(profile).thenAccept {
                                        if (it.type == AcceptResult.Type.SUCCESSFUL) {
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
                finish(0)
            }
        }
    }
}
