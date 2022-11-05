package ink.ptms.chemdah.module.wizard

import ink.ptms.adyeshach.common.entity.EntityInstance
import ink.ptms.chemdah.util.controllerMoveWithPathList
import ink.ptms.chemdah.util.namespace
import org.bukkit.entity.Player
import taboolib.common.platform.function.adaptPlayer
import taboolib.common5.Baffle
import taboolib.module.kether.KetherShell
import taboolib.module.kether.runKether
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

/**
 * Chemdah
 * ink.ptms.chemdah.module.wizard.WizardAction
 *
 * @author 坏黑
 * @since 2022/10/24 19:02
 */
class WizardAction(val player: Player, val entityInstance: EntityInstance, val info: WizardInfo) {

    /** 行为结束时回调 **/
    val onFinish = CompletableFuture<Boolean>()

    /** NPC 状态 **/
    var state = State.MOVING

    /** 事件冷却 **/
    val cooldown = Baffle.of(info.eventCooldown, TimeUnit.MILLISECONDS)

    init {
        // 发起移动
        entityInstance.controllerMoveWithPathList(info.nodes.last(), info.pathList)
    }

    /**
     * NPC 是否可以移动（玩家是否在有效距离内）
     */
    fun shouldMoving(): Boolean {
        return player.world == entityInstance.getWorld() && player.location.distance(entityInstance.getLocation()) < info.waitingDistance
    }

    /**
     * 取消移动
     */
    fun cancel(success: Boolean = false) {
        // 停止移动
        entityInstance.isFreeze = false
        entityInstance.controllerStill()
        // 注销计划
        WizardSystem.actions.remove(entityInstance.uniqueId)
        // 完成脚本
        onFinish.complete(success)
    }

    /**
     * 检查并移动
     */
    fun check(): WizardAction {
        // 终点检测
        if (entityInstance.getLocation().distance(info.nodes.last()) < info.finishDistance) {
            cancel(success = true)
            return this
        }
        // 可以移动
        if (shouldMoving()) {
            entityInstance.isFreeze = false
            // 切换状态执行脚本
            if (state == State.WAITING) {
                state = State.MOVING
                // 不在冷却中
                if (cooldown.hasNext()) {
                    info.eventOnContinue?.let { runKether { KetherShell.eval(it, sender = adaptPlayer(player), namespace = namespace) } }
                }
            }
        } else {
            entityInstance.isFreeze = true
            // 切换状态执行脚本
            if (state == State.MOVING) {
                state = State.WAITING
                // 不在冷却中
                if (cooldown.hasNext()) {
                    info.eventOnWaiting?.let { runKether { KetherShell.eval(it, sender = adaptPlayer(player), namespace = namespace) } }
                }
            }
        }
        return this
    }

    enum class State {

        MOVING, WAITING
    }
}