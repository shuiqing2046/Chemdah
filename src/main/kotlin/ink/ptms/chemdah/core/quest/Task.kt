package ink.ptms.chemdah.core.quest

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.core.DataContainer
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.objective.other.IAlways
import ink.ptms.chemdah.core.quest.objective.other.INever
import taboolib.library.configuration.ConfigurationSection

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.Task
 *
 * @author sky
 * @since 2021/3/1 11:51 下午
 */
class Task(id: String, config: ConfigurationSection, val template: Template) : QuestContainer(id, config) {

    val objective = if (config.contains("objective")) ChemdahAPI.getQuestObjective(config.getString("objective")!!) ?: INever else IAlways
    val condition = DataContainer()
    val goal = DataContainer()

    init {
        config.getConfigurationSection("condition")?.toMap()?.forEach { (k, v) ->
            // 适配 Chemdah Lab
            if (v is String && v.contains(';')) {
                condition[k] = v.split(';').map { it.trim() }
            } else {
                condition[k] = v!!
            }
        }
        config.getConfigurationSection("goal")?.toMap()?.forEach { (k, v) ->
            // 适配 Chemdah Lab
            if (v is String && v.contains(';')) {
                goal[k] = v.split(';').map { it.trim() }
            } else {
                goal[k] = v!!
            }
        }
    }

    fun isCompleted(profile: PlayerProfile) = objective.hasCompletedSignature(profile, this)
}