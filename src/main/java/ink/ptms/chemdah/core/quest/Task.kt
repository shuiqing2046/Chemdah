package ink.ptms.chemdah.core.quest

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.core.DataContainer
import ink.ptms.chemdah.core.DataContainer.Companion.data
import ink.ptms.chemdah.core.quest.objective.other.IAlways
import ink.ptms.chemdah.core.quest.objective.other.INever
import org.bukkit.configuration.ConfigurationSection

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.Task
 *
 * @author sky
 * @since 2021/3/1 11:51 下午
 */
class Task(id: String, config: ConfigurationSection, val template: Template) : QuestContainer(id, config) {

    val objective = if (config.contains("objective")) ChemdahAPI.questObjective[config.getString("objective")] ?: INever else IAlways
    val condition = DataContainer()
    val goal = DataContainer()

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