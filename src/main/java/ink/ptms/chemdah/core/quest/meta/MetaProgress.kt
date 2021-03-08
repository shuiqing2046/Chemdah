package ink.ptms.chemdah.core.quest.meta

import ink.ptms.chemdah.core.quest.Id
import ink.ptms.chemdah.core.quest.QuestContainer
import ink.ptms.chemdah.util.conf
import org.bukkit.configuration.ConfigurationSection

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.meta.MetaProgress
 *
 * @author sky
 * @since 2021/3/9 12:14 上午
 */
@Id("progress")
@MetaType(MetaType.Type.SECTION)
class MetaProgress(source: ConfigurationSection?, questContainer: QuestContainer) : Meta<ConfigurationSection?>(source, questContainer) {

    val visible = source?.getBoolean("visible") ?: false
    val visibleAlways = source?.getBoolean("visible-always") ?: false
    val content = source?.getString("content") ?: conf.getStringColored("settings.default-progress-meta-content")

    companion object {

        fun QuestContainer.progressMeta() = meta<MetaLabel>("progress")
    }
}