package ink.ptms.chemdah.util

import ink.ptms.adyeshach.common.entity.EntityInstance
import ink.ptms.adyeshach.common.entity.ai.general.GeneralGravity
import ink.ptms.adyeshach.common.entity.ai.general.GeneralMove
import ink.ptms.chemdah.AdyeshachChecker
import org.bukkit.Location

/**
 * 使实体根据特定路径移动到某个坐标
 */
fun EntityInstance.controllerMoveWithPathList(location: Location, pathList: List<Location>) {
    if (hasVehicle()) {
        return
    }
    if (AdyeshachChecker.isNewVersion) {
        v2.controllerMoveBy(pathList)
    } else {
        if (getController().none { it is GeneralMove } || getController().none { it is GeneralGravity }) {
            error("Entity walking movement requires GeneralMove and GeneralGravity.")
        }
        val move = getController(GeneralMove::class.java)!!
        move.speed = moveSpeed
        move.target = location
        move.pathType = entityType.getPathType()
        move.resultNavigation = ink.ptms.adyeshach.common.entity.path.ResultNavigation(pathList.map { it.toVector() }, 0L, 0L)
    }
}