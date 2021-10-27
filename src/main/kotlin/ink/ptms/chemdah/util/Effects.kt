package ink.ptms.chemdah.util

import taboolib.common.util.Location
import taboolib.common.util.Vector

/**
 * 粒子算法
 * @author ykdlb
 */
object Effects {

    /**
     * @param start 起始位置
     * @param target 目标方向
     * @param density 粒子密度
     * @param len 箭头长度
     * @param angle 箭头角度
     */
    fun drawArrow(start: Location, target: Location, density: Int, len: Double, angle: Double = 45.0): List<Location> {
        val st = Vector(start.x, start.y, start.z)
        val ed = Vector(target.x, target.y, target.z)
        val dl = ed.clone().subtract(st)
        val length = dl.clone().length()
        val uniV = dl.clone().multiply(1 / length)
        val uniVr = uniV.clone().rotateAroundY(Math.toRadians(angle))
        val uniVl = uniV.clone().rotateAroundY(Math.toRadians(-angle))
        val l = len / density.toDouble()
        val result = ArrayList<Location>()
        result.add(st.clone().add(uniV).toLocation(start.world!!))
        (0 until density).forEach { i ->
            val point = st.clone().add(uniV).subtract(uniVr.clone().multiply(l * (i + 1)))
            val point2 = st.clone().add(uniV).subtract(uniVl.clone().multiply(l * (i + 1)))
            result.add(point.toLocation(start.world!!))
            result.add(point2.toLocation(start.world!!))
        }
        return result
    }
}