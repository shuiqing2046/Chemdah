package ink.ptms.chemdah.module.wizard

import ink.ptms.adyeshach.common.entity.EntityInstance
import org.bukkit.Location
import org.bukkit.entity.Player
import taboolib.common5.Baffle

/**
 * Chemdah
 * ink.ptms.chemdah.module.wizard.WizardAction
 *
 * @author 坏黑
 * @since 2022/10/24 19:02
 */
class WizardAction(val player: Player, val entityInstance: EntityInstance, val info: WizardInfo) {

    var currentNode: Location? = null

    fun shouldMoving(): Boolean {
        return player.world == entityInstance.getWorld() && player.location.distance(entityInstance.getLocation()) < info.waitingDistance
    }

    fun check(): WizardAction {
        if (shouldMoving()) {
            val nearestNode = info.getNearestNode(entityInstance.getLocation())
            if (nearestNode != null && nearestNode != currentNode) {
                currentNode = nearestNode
                entityInstance.controllerMove(nearestNode)
            }
        } else {
            currentNode = null
            entityInstance.controllerStill()
        }
        return this
    }
}