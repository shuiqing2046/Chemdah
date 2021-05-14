package ink.ptms.chemdah.module.scenes

import ink.ptms.chemdah.util.asInt
import ink.ptms.chemdah.util.asMap
import org.bukkit.configuration.ConfigurationSection

/**
 * Chemdah
 * ink.ptms.chemdah.module.scenes.ScenesFile
 *
 * @author sky
 * @since 2021/5/13 11:50 下午
 */
class ScenesFile(val root: ConfigurationSection) {

    val world = root.getString("world")
    val state = root.getConfigurationSection("state")?.getKeys(false)?.mapNotNull {
        when {
            root.contains("state.set") -> ScenesState.Block(it.asInt(), root)
            root.contains("state.copy") -> ScenesState.Copy(it.asInt(), root)
            else -> null
        }
    }
    val automation = root.getMapList("automation").map {
        ScenesAutomation(it.asMap())
    }
}