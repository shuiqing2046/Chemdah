package ink.ptms.chemdah.core.quest

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.core.DataContainer
import ink.ptms.chemdah.core.DataContainer.Companion.data
import ink.ptms.chemdah.core.quest.addon.Addon
import ink.ptms.chemdah.core.quest.meta.Meta
import ink.ptms.chemdah.core.quest.meta.MetaContainer
import org.bukkit.configuration.ConfigurationSection

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.Task
 *
 * @author sky
 * @since 2021/3/1 11:51 下午
 */
class Task(val id: String, val config: ConfigurationSection, val template: Template) : MetaContainer {

    val objective = ChemdahAPI.questObjective[config.getString("objective").toString()]!!
    val condition = DataContainer()
    val goal = DataContainer()
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