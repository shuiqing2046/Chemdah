package ink.ptms.chemdah.core.quest

import taboolib.common.platform.function.warning
import taboolib.library.configuration.ConfigurationSection

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
                    ANY -> config[node]
                }
            } catch (e: Throwable) {
                warning("${config[node]} (${config[node]?.javaClass?.simpleName}) cannot cast to $this ($node)")
            }
            return null
        }
    }
}
