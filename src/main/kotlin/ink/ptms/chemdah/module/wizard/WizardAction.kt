package ink.ptms.chemdah.module.wizard

import ink.ptms.adyeshach.common.entity.EntityInstance
import ink.ptms.chemdah.util.namespace
import org.bukkit.Location
import org.bukkit.entity.Player
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.platform.function.warning
import taboolib.module.kether.KetherShell
import taboolib.module.kether.runKether
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.module.wizard.WizardAction
 *
 * @author 坏黑
 * @since 2022/10/24 19:02
 */
class WizardAction(val player: Player, val entityInstance: EntityInstance, val info: WizardInfo) {

    val finishFuture = CompletableFuture<Boolean>()
    var currentNode: Location? = null

    /** NPC 状态 **/
    var state = State.MOVING

    fun shouldMoving(): Boolean {
        return player.world == entityInstance.getWorld() && player.location.distance(entityInstance.getLocation()) < info.waitingDistance
    }

    fun cancel(success: Boolean = false) {
        WizardSystem.actions.remove(entityInstance.uniqueId)
        finishFuture.complete(success)
    }

    fun check(): WizardAction {
        if (info.nodes.isEmpty()) {
            warning("[${info.id}] Wizard nodes is empty.")
            cancel()
            return this
        }
        if (entityInstance.getLocation().distance(info.nodes.last()) < info.finishDistance) {
            cancel(success = true)
            return this
        }
        if (shouldMoving()) {
            val nearestNode = info.getNearestNode(entityInstance.getLocation())
            if (nearestNode != null) {
                currentNode = nearestNode
                entityInstance.controllerMove(nearestNode)
                // 切换状态执行脚本
                if (state == State.WAITING) {
                    state = State.MOVING
                    info.eventOnContinue?.let { runKether { KetherShell.eval(it, sender = adaptPlayer(player), namespace = namespace) } }
                }
            }
        } else {
            currentNode = null
            entityInstance.controllerStill()
            // 切换状态执行脚本
            if (state == State.MOVING) {
                state = State.WAITING
                info.eventOnWaiting?.let { runKether { KetherShell.eval(it, sender = adaptPlayer(player), namespace = namespace) } }
            }
        }
        return this
    }

    enum class State {

        MOVING, WAITING
    }
}