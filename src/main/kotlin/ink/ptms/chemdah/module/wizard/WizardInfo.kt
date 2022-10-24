package ink.ptms.chemdah.module.wizard

import ink.ptms.adyeshach.common.entity.EntityInstance
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import taboolib.common.platform.function.warning
import taboolib.library.configuration.ConfigurationSection

/**
 * Chemdah
 * ink.ptms.chemdah.module.wizard.WizardInfo
 *
 * @author 坏黑
 * @since 2022/10/23 08:34
 */
class WizardInfo(val root: ConfigurationSection) {

    /** 世界名 **/
    val world = Bukkit.getWorld(root.getString("in").toString())

    /** 节点 **/
    val nodes = root.getStringList("nodes").map { Location(world, it.split(" ")[0].toDouble(), it.split(" ")[1].toDouble(), it.split(" ")[2].toDouble()) }

    /** 等待距离 **/
    val waitingDistance = root.getDouble("waiting-distance")

    /** 有效距离修正 **/
    val effectiveDistanceCorrections = root.getDouble("effective-distance-corrections")

    /**
     * Apply
     *
     * @param player 玩家
     * @param entityInstance 单位实例
     */
    fun apply(player: Player, entityInstance: EntityInstance) {
        WizardSystem.actions[entityInstance.uniqueId] = WizardAction(player, entityInstance, this).check()
    }

    /**
     * Get nearest node
     *
     * @param location 单位坐标
     * @return [Location]
     */
    fun getNearestNode(location: Location): Location? {
        if (nodes.isEmpty()) {
            warning("Wizard nodes is empty.")
            return null
        }
        // 获取目的地
        val destination = nodes.last()
        // 获取目的地与当前位置的距离
        val valid = location.distance(destination) - effectiveDistanceCorrections
        // 获取目的地与当前位置范围内的节点
        // 获取最远移动节点
        val next = nodes.filter { it.distance(destination) < valid }.filter { it.distance(location) < 32 }.maxByOrNull { it.distance(location) }
        if (next == null) {
            warning("No nearest node found.")
            return null
        }
        return next
    }
}