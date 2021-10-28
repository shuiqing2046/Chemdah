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
}

class PlanTypeHour(realTime: RealTime, unit: RealTime.Type, value: Int) : PlanType(realTime, unit, value) {

    override val nextTime = Calendar.getInstance().run {
        timeInMillis = realTime.nextTime(RealTime.Type.HOUR, value)
        timeInMillis
    }
}

class PlanTypeDaily(realTime: RealTime, unit: RealTime.Type, value: Int, hour: Int, minute: Int) : PlanType(realTime, unit, value) {

    override val nextTime = Calendar.getInstance().run {
        timeInMillis = realTime.nextTime(RealTime.Type.DAY, value)
        add(Calendar.HOUR, hour)
        add(Calendar.MINUTE, minute)
        timeInMillis
    }
}

class PlanTypeWeekly(realTime: RealTime, unit: RealTime.Type, value: Int, day: Int, hour: Int, minute: Int) : PlanType(realTime, unit, value) {

    override val nextTime = Calendar.getInstance().run {
        timeInMillis = realTime.nextTime(RealTime.Type.WEEK, value)
        add(Calendar.DAY_OF_WEEK, day)
        add(Calendar.HOUR, hour)
        add(Calendar.MINUTE, minute)
        timeInMillis
    }
}

abstract class PlanType(val realTime: RealTime, val unit: RealTime.Type, val value: Int) {

    abstract val nextTime: Long
}