package ink.ptms.chemdah.core.quest.addon.data

import com.google.common.base.Enums
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Template
import ink.ptms.chemdah.core.quest.meta.MetaType.Companion.type
import ink.ptms.chemdah.util.namespaceQuest
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.util.asList
import taboolib.common5.Coerce
import taboolib.common5.TimeCycle
import taboolib.module.kether.KetherShell
import taboolib.module.kether.printKetherErrorMessage
import java.util.concurrent.CompletableFuture

/**
 * 管控结果
 *
 * @param pass 是否通过
 * @param reason 理由
 */
data class ControlResult(val pass: Boolean, val reason: String? = null)

/**
 * 管控触发器
 */
enum class ControlTrigger {

    ACCEPT, FAIL, COMPLETE;

    companion object {

        /** 从字符串获取枚举 */
        fun fromName(name: String) = Enums.getIfPresent(ControlTrigger::class.java, name.uppercase()).or(COMPLETE)!!
    }
}

/**
 * 管控
 */
abstract class Control {

    abstract val trigger: ControlTrigger?

    abstract fun check(profile: PlayerProfile, template: Template): CompletableFuture<ControlResult>

    abstract fun signature(profile: PlayerProfile, template: Template)

    protected fun Boolean.toResult(reason: String): ControlResult {
        return ControlResult(this, reason)
    }
}

/**
 * 管控：脚本代理
 */
open class ControlAgent(val agent: List<String>) : Control() {

    override val trigger: ControlTrigger?
        get() = null

    override fun check(profile: PlayerProfile, template: Template): CompletableFuture<ControlResult> {
        return try {
            KetherShell.eval(agent.asList(), sender = adaptPlayer(profile.player), namespace = namespaceQuest) {
                set("@QuestContainer", template)
            }.thenApply {
                Coerce.toBoolean(it).toResult("agent")
            }
        } catch (e: Throwable) {
            e.printKetherErrorMessage()
            CompletableFuture.completedFuture(ControlResult(false, "agent"))
        }
    }

    override fun signature(profile: PlayerProfile, template: Template) {
    }
}

/**
 * 管控：冷却
 *
 * @param type 管控触发器类型
 * @param time 间隔
 * @param group 组
 */
open class ControlCooldown(val type: ControlTrigger, val time: TimeCycle, val group: String?) : Control() {

    override val trigger: ControlTrigger
        get() = type

    override fun check(profile: PlayerProfile, template: Template): CompletableFuture<ControlResult> {
        val id = "quest.cooldown.${if (group != null) "@$group" else template.id}.${type.name.lowercase()}"
        val start = profile.persistentDataContainer[id, 0L].toLong()
        return CompletableFuture.completedFuture(time.start(start).isTimeout(start).toResult("cooldown"))
    }

    override fun signature(profile: PlayerProfile, template: Template) {
        val id = "quest.cooldown.${if (group != null) "@$group" else template.id}.${type.name.lowercase()}"
        profile.persistentDataContainer[id] = System.currentTimeMillis()
    }
}

/**
 * 管控：共存
 *
 * @param type 标签与数量
 */
class ControlCoexist(val type: Map<String, Int>) : Control() {

    override val trigger: ControlTrigger?
        get() = null

    override fun check(profile: PlayerProfile, template: Template): CompletableFuture<ControlResult> {
        return if (type.any { label -> profile.getQuests().count { label.key in it.template.type() } >= label.value }) {
            CompletableFuture.completedFuture(ControlResult(false, "coexist"))
        } else {
            CompletableFuture.completedFuture(ControlResult(true, "coexist"))
        }
    }

    override fun signature(profile: PlayerProfile, template: Template) {
    }
}

/**
 * 管控：重复
 *
 * @param type 管控触发器类型
 * @param amount 重复数量
 * @param period 刷新周期
 * @param group 组
 */
open class ControlRepeat(val type: ControlTrigger, val amount: Int, val period: TimeCycle?, val group: String?) : Control() {

    override val trigger: ControlTrigger
        get() = type

    override fun check(profile: PlayerProfile, template: Template): CompletableFuture<ControlResult> {
        val id = "quest.repeat.${if (group != null) "@$group" else template.id}.${type.name.lowercase()}"
        val time = profile.persistentDataContainer["$id.time", 0L].toLong()
        // 超出重复限时
        if (period != null && period.start(time).isTimeout(time)) {
            return CompletableFuture.completedFuture(ControlResult(true, "repeat"))
        }
        return CompletableFuture.completedFuture((profile.persistentDataContainer["$id.amount", 0].toInt() < amount).toResult("repeat"))
    }

    override fun signature(profile: PlayerProfile, template: Template) {
        val id = "quest.repeat.${if (group != null) "@$group" else template.id}.${type.name.lowercase()}"
        val time = profile.persistentDataContainer["$id.time", 0L].toLong()
        // 超出重复限时
        if (period != null && period.start(time).isTimeout(time)) {
            // 初始化变量
            profile.persistentDataContainer["$id.amount"] = 1
            profile.persistentDataContainer["$id.time"] = System.currentTimeMillis()
        } else {
            // 追加次数
            profile.persistentDataContainer["$id.amount"] = profile.persistentDataContainer["$id.amount", 0].toInt() + 1
        }
    }
}

/**
 * 管控控制器
 */
open class ControlOperator(val template: Template, val control: List<Control>?) {

    /**
     * 任务是否被限制接受
     */
    fun check(profile: PlayerProfile): CompletableFuture<ControlResult> {
        val future = CompletableFuture<ControlResult>()
        if (control == null) {
            future.complete(ControlResult(true))
            return future
        }
        fun process(cur: Int) {
            if (cur < control.size) {
                control[cur].check(profile, template).thenApply {
                    if (it.pass) {
                        process(cur + 1)
                    } else {
                        future.complete(it)
                    }
                }
            } else {
                future.complete(ControlResult(true))
            }
        }
        process(0)
        return future
    }

    fun signature(profile: PlayerProfile, type: ControlTrigger = ControlTrigger.COMPLETE) {
        control?.filter { it.trigger == null || it.trigger == type }?.forEach { it.signature(profile, template) }
    }
}