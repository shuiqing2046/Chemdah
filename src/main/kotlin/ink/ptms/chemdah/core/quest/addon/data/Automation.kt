package ink.ptms.chemdah.core.quest.addon.data

import ink.ptms.chemdah.core.quest.Template
import taboolib.common5.RealTime
import java.util.*

/**
 * 计划组
 *
 * @param groupId 组名
 * @param plan 计划配置
 */
class PlanGroup(val groupId: String, val plan: Plan) {

    val quests = ArrayList<Template>()
}

/**
 * 计划配置
 *
 * @param type 计划类型
 * @param count 计划间隔
 * @param group 计划组
 */
class Plan(val type: PlanType, val count: Int, val group: String?) {

    /** 下次计划事件 */
    val nextTime: Long
        get() = type.nextTime

    val debug: String
        get() = "Plan(type=$type, count=$count, group=$group)"
}

/**
 * 计划类型
 */
abstract class PlanType(val realTime: RealTime, val unit: RealTime.Type, var value: Int) {

    /** 下次计划时间 */
    abstract val nextTime: Long
}

/**
 * Hour 类型计划
 *
 * @param realTime 时间类型
 * @param unit 时间单位
 * @param value 基本时间周期
 */
open class PlanTypeHour(realTime: RealTime, unit: RealTime.Type, value: Int) : PlanType(realTime, unit, value) {

    /** 下次计划时间 */
    override val nextTime: Long
        get() = Calendar.getInstance().run {
            timeInMillis = realTime.nextTime(RealTime.Type.HOUR, value)
            timeInMillis
        }

    override fun toString(): String {
        return "PlanTypeHour(realTime=$realTime, unit=$unit, value=$value)"
    }
}

/**
 * Daily 类型计划
 *
 * @param realTime 时间类型
 * @param unit 时间单位
 * @param value 基本时间周期
 * @param hour 小时
 * @param minute 分钟
 */
open class PlanTypeDaily(realTime: RealTime, unit: RealTime.Type, value: Int, val hour: Int, val minute: Int) : PlanType(realTime, unit, value) {

    /** 下次计划时间 */
    override val nextTime: Long
        get() = Calendar.getInstance().run {
            timeInMillis = realTime.nextTime(RealTime.Type.DAY, value)
            add(Calendar.HOUR, hour)
            add(Calendar.MINUTE, minute)
            timeInMillis
        }

    override fun toString(): String {
        return "PlanTypeDaily(realTime=$realTime, unit=$unit, value=$value, hour=$hour, minute=$minute)"
    }
}

/**
 * Weekly 类型计划
 *
 * @param realTime 时间类型
 * @param unit 时间单位
 * @param value 基本时间周期
 * @param day 天
 * @param hour 小时
 * @param minute 分钟
 */
open class PlanTypeWeekly(realTime: RealTime, unit: RealTime.Type, value: Int, val day: Int, val hour: Int, val minute: Int) : PlanType(realTime, unit, value) {

    /** 下次计划时间 */
    override val nextTime: Long
        get() = Calendar.getInstance().run {
            timeInMillis = realTime.nextTime(RealTime.Type.WEEK, value)
            add(Calendar.DAY_OF_WEEK, day)
            add(Calendar.HOUR, hour)
            add(Calendar.MINUTE, minute)
            timeInMillis
        }

    override fun toString(): String {
        return "PlanTypeWeekly(realTime=$realTime, unit=$unit, value=$value, day=$day, hour=$hour, minute=$minute)"
    }
}