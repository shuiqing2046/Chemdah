package ink.ptms.chemdah.core.quest

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.core.Metadata
import ink.ptms.chemdah.core.Metadata.Companion.data
import ink.ptms.chemdah.core.quest.addon.Addon
import ink.ptms.chemdah.core.quest.meta.Meta
import org.bukkit.configuration.ConfigurationSection

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.Task
 *
 * @author sky
 * @since 2021/3/1 11:51 下午
 */
class Task(val id: String, val config: ConfigurationSection, val template: Template) : Container {

    val objective = ChemdahAPI.questObjective[config.getString("objective").toString()]!!
    val condition = Metadata()
    val goal = Metadata()
    val meta = HashMap<String, Meta>()
    val addon = HashMap<String, Addon>()

    val metaNode = "${template.id}.$id"

    init {
        config.getConfigurationSection("if")?.getValues(false)?.forEach { (k, v) ->
            condition.put(k, v.data())
        }
        config.getConfigurationSection("when")?.getValues(false)?.forEach { (k, v) ->
            goal.put(k, v.data())
        }
    }
}