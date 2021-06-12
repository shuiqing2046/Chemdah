package ink.ptms.chemdah.module.scenes

import ink.ptms.chemdah.util.asInt
import org.bukkit.configuration.ConfigurationSection

/**
 * Chemdah
 * ink.ptms.chemdah.module.scenes.ScenesFile
 *
 * @author sky
 * @since 2021/5/13 11:50 下午
 */
class ScenesFile(val root: ConfigurationSection) {

    val world = root.getString("world") ?: "world"
    val state = root.getConfigurationSection("state")?.getKeys(false)?.mapNotNull {
        when {
            root.contains("state.set") -> ScenesState.Block(it.asInt(), root, this)
            root.contains("state.copy") -> ScenesState.Copy(it.asInt(), root, this)
            else -> null
        }
    } ?: emptyList()
}