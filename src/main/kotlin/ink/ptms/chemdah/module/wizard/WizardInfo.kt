package ink.ptms.chemdah.module.wizard

import ink.ptms.adyeshach.common.entity.EntityInstance
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import taboolib.common.platform.function.warning
import taboolib.common5.util.parseMillis
import taboolib.library.configuration.ConfigurationSection
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

    /** 有效距离修正 **/
    val effectiveDistanceCorrections = root.getDouble("effective-distance-corrections", 2.0)

    /** 当 NPC 暂停时触发脚本 **/
    val eventOnWaiting = root.getString("event.waiting")

    /** 当 NPC 继续时触发脚本 **/
    val eventOnContinue = root.getString("event.continue")

    /** 事件冷却 **/
    val eventCooldown = root.getString("event-cooldown")?.parseMillis() ?: 0

    /** 关闭对话 **/
    val disableConversation = root.getBoolean("disable-conversation")

    /**
     * Apply
     *
     * @param player 玩家
     * @param entityInstance 单位实例
     */
    fun apply(player: Player, entityInstance: EntityInstance): CompletableFuture<Boolean> {
        val action = WizardAction(player, entityInstance, this).check()
        WizardSystem.actions[entityInstance.uniqueId] = action
        return action.onFinish
    }

    /**
     * Get nearest node
     *
     * @param location 单位坐标
     * @return [Location]
     */
    fun getNearestNode(location: Location): Location? {
        if (nodes.isEmpty()) {
            warning("[$id] Wizard nodes is empty.")
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
            warning("[$id] No nearest node found.")
            return null
        }
        return next
    }
}