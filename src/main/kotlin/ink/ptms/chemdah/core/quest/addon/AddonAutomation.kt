package ink.ptms.chemdah.core.quest.addon

import com.google.common.base.Enums
import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.api.ChemdahAPI.chemdahProfile
import ink.ptms.chemdah.api.ChemdahAPI.isChemdahProfileLoaded
import ink.ptms.chemdah.core.quest.*
import ink.ptms.chemdah.core.quest.addon.data.*
import org.bukkit.Bukkit
import taboolib.common.platform.Schedule
import taboolib.common.platform.function.warning
import taboolib.common5.Coerce
import taboolib.common5.RealTime
import taboolib.library.configuration.ConfigurationSection
import java.util.Date
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

    val isAutoAccept = source.getBoolean("auto-accept")

    val plan = if (source.contains("plan")) {
        val method = Enums.getIfPresent(RealTime::class.java, source.getString("plan.method").toString().uppercase()).or(RealTime.START_IN_MONDAY)
        val args = source.getString("plan.type").toString().lowercase().split(" ")
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
            if (type.value == 0) {
                type.value = 1
                warning("[Automation] 任务 ${questContainer.id} 中的计划周期为 0，已自动修正。")
                warning("[Automation] 配置:")
                source.toString().lines().forEach {
                    warning("[Automation]  | $it")
                }
            }
            Plan(type, source.getInt("plan.count", 1), source.getString("plan.group"))
        } else {
            null
        }
    } else {
        null
    }

    val planGroup: String? = source.getString("plan.group")

    companion object {

        fun Template.isAutoAccept() = addon<AddonAutomation>("automation")?.isAutoAccept ?: false

        fun Template.plan() = addon<AddonAutomation>("automation")?.plan

        fun Template.planGroup() = addon<AddonAutomation>("automation")?.planGroup

        @Schedule(period = 40, async = true)
        fun automation40() {
            if (Bukkit.getOnlinePlayers().isEmpty()) {
                return
            }
            val groups = HashMap<String, PlanGroup>()
            val autoAccept = ArrayList<Template>()
            // 优先加载拥有主动逻辑的 Plan 计划
            ChemdahAPI.questTemplate.forEach { (_, quest) ->
                if (quest.isAutoAccept()) {
                    autoAccept.add(quest)
                } else {
                    val plan = quest.plan()
                    if (plan != null) {
                        val id = if (plan.group != null) "@${plan.group}" else quest.id
                        val group = groups.computeIfAbsent(id) { PlanGroup(id, plan) }
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
            Bukkit.getOnlinePlayers().filter { it.isChemdahProfileLoaded }.forEach { player ->
                val profile = player.chemdahProfile
                // 自动接受的任务
                autoAccept.forEach {
                    if (profile.getQuestById(it.id, openAPI = false) == null) {
                        it.acceptTo(profile)
                    }
                }
                // 定时计划
                groups.forEach self@{ (id, group) ->
                    val nextTime = profile.persistentDataContainer["quest.automation.$id.next", 0L].toLong()
                    if (nextTime < System.currentTimeMillis()) {
                        val newTime = group.plan.nextTime
                        if (newTime < System.currentTimeMillis()) {
                            val now = Date(System.currentTimeMillis())
                            warning("[Automation] $id 的计划时间已过期, 无法分发任务: ${Date(newTime)}, 当前时间: $now")
                            warning("[Automation] 调试: ${group.plan.debug}")
                            return@self
                        }
                        profile.persistentDataContainer["quest.automation.$id.next"] = newTime
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
        }
    }
}
