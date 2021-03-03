package ink.ptms.chemdah.core.quest

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.core.quest.meta.Meta
import ink.ptms.chemdah.core.quest.meta.MetaContainer
import org.bukkit.configuration.ConfigurationSection

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.Template
 *
 * @author sky
 * @since 2021/3/1 11:43 下午
 */
class Template(val id: String, val config: ConfigurationSection) : MetaContainer {

    val task = HashMap<String, Task>()
    val meta = HashMap<String, Meta>()

    private val cloneMeta = config.getString("meta.clone")

    init {
        config.getConfigurationSection("meta")?.getKeys(false)?.forEach {

        }
    }

    /**
     * 获取包含克隆模板的所有任务元数据
     * 已配置的元数据会覆盖克隆源
     */
    fun metaAll(): Map<String, Meta> {
        return HashMap<String, Meta>().also {
            if (cloneMeta != null) {
                ChemdahAPI.getQuestTemplate(cloneMeta)?.metaAll()?.run { it.putAll(this) }
            }
            it.putAll(meta)
        }
    }
}