package ink.ptms.chemdah.module.wizard

import ink.ptms.adyeshach.common.entity.EntityInstance
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import taboolib.common.platform.function.warning
import taboolib.common5.Coerce
import taboolib.common5.util.parseMillis
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.navigation.NodeEntity
import taboolib.module.navigation.createPathfinder
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.module.wizard.WizardInfo
 *
 * @author 坏黑
 * @since 2022/10/23 08:34
 */
class WizardInfo(val root: ConfigurationSection) {

    /** 序号 **/
    val id = root.name

    /** 世界名 **/
    val world = Bukkit.getWorld(root.getString("in").toString())

    /** 节点 **/
    val nodes = root.getStringList("nodes").map { Location(world, it.split(" ")[0].toDouble(), it.split(" ")[1].toDouble(), it.split(" ")[2].toDouble()) }

    /** 结束距离 **/
    val finishDistance = root.getDouble("finish-distance", 2.0)

    /** 等待距离 **/
    val waitingDistance = root.getDouble("waiting-distance")

    /** 当 NPC 暂停时触发脚本 **/
    val eventOnWaiting = root.getString("event.waiting")

    /** 当 NPC 继续时触发脚本 **/
    val eventOnContinue = root.getString("event.continue")

    /** 事件冷却 **/
    val eventCooldown = root.getString("event-cooldown")?.parseMillis() ?: 0

    /** 关闭对话 **/
    val disableConversation = root.getBoolean("disable-conversation")

    /** 路径列表 **/
    val pathList = pathList0()

    /** 路径列表是否连续有效 **/
    val pathListValid = pathListValid0()

    /**
     * Apply
     *
     * @param player 玩家
     * @param entityInstance 单位实例
     */
    fun apply(player: Player, entityInstance: EntityInstance): CompletableFuture<Boolean> {
        return if (pathListValid) {
            val action = WizardAction(player, entityInstance, this).check()
            WizardSystem.actions[entityInstance.uniqueId] = action
            action.onFinish
        } else {
            warning("[Wizard] Invalid path list: $id")
            CompletableFuture.completedFuture(false)
        }
    }

    /**
     * 获取路径列表
     */
    private fun pathList0(): List<Location> {
        val path = mutableListOf<Location>()
        for (i in 0 until nodes.size - 1) {
            val a = nodes[i]
            val b = nodes[i + 1]
            val pathList = createPathfinder(NodeEntity(a, 2.0)).findPath(b, 32f)?.nodes
            if (pathList != null) {
                path += pathList.map { it.asBlockPos().toLocation(world!!) }
            }
        }
        return path
    }

    /**
     * 检查路径是否连续有效
     */
    private fun pathListValid0(): Boolean {
        for (i in 0 until pathList.size - 1) {
            val a = pathList[i]
            val b = pathList[i + 1]
            if (a.distance(b) > 2.0) {
                val at = "${a.blockX},${a.blockY},${a.blockZ}"
                val bt = "${b.blockX},${b.blockY},${b.blockZ}"
                warning("[Wizard] Discontinuous path list: $id ($at -> $bt distance: ${Coerce.format(a.distance(b))})")
                return false
            }
        }
        return pathList.isNotEmpty()
    }
}