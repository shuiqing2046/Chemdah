package ink.ptms.chemdah.module.wizard

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
    val world = root.getString("in").toString()

    /** 节点 **/
    val nodes = root.getStringList("nodes").map { Node(it.split(" ")[0].toInt(), it.split(" ")[1].toInt(), it.split(" ")[2].toInt()) }

    /** 等待距离 **/
    val waitingDistance = root.getDouble("waiting-distance")
}