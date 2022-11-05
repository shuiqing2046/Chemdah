package ink.ptms.chemdah.util

import ink.ptms.adyeshach.common.entity.EntityInstance
import ink.ptms.adyeshach.common.entity.ai.general.GeneralGravity
import ink.ptms.adyeshach.common.entity.ai.general.GeneralMove
import ink.ptms.adyeshach.common.entity.path.PathType
import ink.ptms.adyeshach.common.entity.path.ResultNavigation
import org.bukkit.Location

/**
 * 使实体根据特定路径移动到某个坐标
 */
fun EntityInstance.controllerMoveWithPathList(
    location: Location,
    pathList: List<Location>,
    pathType: PathType = entityType.getPathType(),
    speed: Double = moveSpeed,
) {
    if (hasVehicle()) {
        return
    }
    if (pathType == PathType.FLY) {
        if (getController().none { it is GeneralMove }) {
            error("Entity flying movement requires GeneralMove.")
        }
    } else {
        if (getController().none { it is GeneralMove } || getController().none { it is GeneralGravity }) {
            error("Entity walking movement requires GeneralMove and GeneralGravity.")
        }
    }
    val move = getController(GeneralMove::class.java)!!
    move.speed = speed
    move.target = location
    move.pathType = pathType
    move.resultNavigation = ResultNavigation(pathList.map { it.toVector() }, 0L, 0L)
}