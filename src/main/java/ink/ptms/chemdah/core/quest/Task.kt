package ink.ptms.chemdah.core.quest

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.core.DataContainer
import ink.ptms.chemdah.core.DataContainer.Companion.data
import ink.ptms.chemdah.core.quest.addon.Addon
import io.izzel.taboolib.util.Reflection
import org.bukkit.configuration.ConfigurationSection

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.Task
 *
 * @author sky
 * @since 2021/3/1 11:51 下午
 */
class Task(val id: String, config: ConfigurationSection, val template: Template) : QuestContainer(config) {

    val objective = ChemdahAPI.questObjective[config.getString("objective").toString()]!!
    val condition = DataContainer()
    val goal = DataContainer()
    val addon = HashMap<String, Addon>()

    val metaNode = "${template.id}.$id"

    init {
        config.getConfigurationSection("if")?.getValues(false)?.forEach { (k, v) ->
            condition.put(k, v.data())
        }
        config.getConfigurationSection("when")?.getValues(false)?.forEach { (k, v) ->
            goal.put(k, v.data())
        }
        config.getKeys(false)
            .filter { it.startsWith("apply(") && it.endsWith(")") }
            .forEach {
                val addonId = it.substring("apply(".length, it.length - 1)
                val addon = ChemdahAPI.getQuestAddon(addonId)
                if (addon != null) {
                    this.addon[addonId] = Reflection.instantiateObject(addon, config.getConfigurationSection(it)!!, this) as Addon
                }
            }
    }
}