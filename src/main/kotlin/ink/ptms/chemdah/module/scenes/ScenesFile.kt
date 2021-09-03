package ink.ptms.chemdah.module.scenes

import ink.ptms.chemdah.util.asInt
import taboolib.library.configuration.ConfigurationSection

/**
 * Chemdah
 * ink.ptms.chemdah.module.scenes.ScenesFile
 *
 * @author sky
 * @since 2021/5/13 11:50 下午
 */
class ScenesFile(val root: ConfigurationSection) {

    val world = root.getString("in") ?: "world"
    val state = root.getConfigurationSection("state")?.getKeys(false)?.mapNotNull {
        when {
            root.contains("state.$it.set") -> ScenesState.Block(it.asInt(), root.getConfigurationSection("state.$it")!!, this)
            root.contains("state.$it.copy") -> ScenesState.Copy(it.asInt(), root.getConfigurationSection("state.$it")!!, this)
            else -> null
        }
    } ?: emptyList()
}