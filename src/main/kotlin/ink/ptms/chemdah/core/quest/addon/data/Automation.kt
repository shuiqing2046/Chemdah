package ink.ptms.chemdah.core.quest.addon.data

import ink.ptms.chemdah.core.quest.Template
import taboolib.common5.RealTime
import java.util.*

class PlanGroup(val groupId: String, val plan: Plan) {

    val quests = ArrayList<Template>()
}

class Plan(val type: PlanType, val count: Int, val group: String?) {

    val nextTime: Long
        get() = type.nextTime

    val debug: String
        get() = "Plan(type=$type, count=$count, group=$group)"
}

class PlanTypeHour(realTime: RealTime, unit: RealTime.Type, value: Int) : PlanType(realTime, unit, value) {

    override val nextTime: Long
        get() = Calendar.getInstance().run {
            timeInMillis = realTime.nextTime(RealTime.Type.HOUR, value)
            timeInMillis
        }

    override fun toString(): String {
        return "PlanTypeHour(realTime=$realTime, unit=$unit, value=$value)"
    }
}

class PlanTypeDaily(realTime: RealTime, unit: RealTime.Type, value: Int, val hour: Int, val minute: Int) : PlanType(realTime, unit, value) {

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

class PlanTypeWeekly(realTime: RealTime, unit: RealTime.Type, value: Int, val day: Int, val hour: Int, val minute: Int) : PlanType(realTime, unit, value) {

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

abstract class PlanType(val realTime: RealTime, val unit: RealTime.Type, var value: Int) {

    abstract val nextTime: Long
}