package ink.ptms.chemdah.core.quest

import ink.ptms.chemdah.util.warning
import org.bukkit.configuration.ConfigurationSection

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.MetaType
 *
 * @author sky
 * @since 2021/3/4 12:45 上午
 */
annotation class Option(val type: Type) {

    enum class Type {

        LIST, MAP_LIST, SECTION, TEXT, NUMBER, ANY;

        operator fun get(config: ConfigurationSection, node: String): Any? {
            try {
                return when (this) {
                    LIST -> config.getList(node)
                    MAP_LIST -> config.getMapList(node)
                    SECTION -> config.getConfigurationSection(node)
                    TEXT -> config.getString(node)
                    NUMBER -> config.getDouble(node)
                    ANY -> config.get(node)
                }
            } catch (e: Throwable) {
                warning("${config.get(node)} (${config.get(node)?.javaClass?.simpleName}) cannot cast to $this ($node)")
            }
            return null
        }
    }
}
