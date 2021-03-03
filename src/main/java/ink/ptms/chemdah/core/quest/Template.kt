package ink.ptms.chemdah.core.quest

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.core.quest.meta.Meta
import org.bukkit.configuration.ConfigurationSection

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.Template
 *
 * @author sky
 * @since 2021/3/1 11:43 下午
 */
class Template(val id: String, val config: ConfigurationSection) : Container {

    val task = HashMap<String, Task>()
    val meta = HashMap<String, Meta>()

    private val cloneMeta = config.getString("meta.clone")

    init {
        config.getConfigurationSection("meta")?.getKeys(false)?.forEach {

        }
    }

    fun metaAll(): Map<String, Meta> {
        return HashMap<String, Meta>().also {
            if (cloneMeta != null) {
                ChemdahAPI.getQuestTemplate(cloneMeta)?.metaAll()?.run { it.putAll(this) }
            }
            it.putAll(meta)
        }
    }
}