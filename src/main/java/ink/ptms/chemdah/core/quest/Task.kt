package ink.ptms.chemdah.core.quest

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.core.DataContainer
import ink.ptms.chemdah.core.DataContainer.Companion.data
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.addon.Addon
import ink.ptms.chemdah.core.quest.addon.AddonStats.Companion.stats
import ink.ptms.chemdah.core.quest.objective.IAlways
import ink.ptms.chemdah.core.quest.objective.INever
import ink.ptms.chemdah.core.quest.objective.Progress
import io.izzel.taboolib.util.Reflection
import org.bukkit.configuration.ConfigurationSection
import java.util.concurrent.CompletableFuture

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
    val addons = HashMap<String, Addon>()

    val metaNode = "${template.id}.$id"

    init {
        config.getConfigurationSection("if")?.getValues(false)?.forEach { (k, v) ->
            condition.put(k, v.data())
        }
        config.getConfigurationSection("when")?.getValues(false)?.forEach { (k, v) ->
            goal.put(k, v.data())
        }
        config.getKeys(false)
            .filter { it.startsWith("addon(") && it.endsWith(")") }
            .forEach {
                val addonId = it.substring("addon(".length, it.length - 1)
                val addon = ChemdahAPI.getQuestAddon(addonId)
                if (addon != null) {
                    this.addons[addonId] = Reflection.instantiateObject(addon, config.getConfigurationSection(it)!!, this) as Addon
                }
            }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Addon> addon(addonId: String): T? {
        return addons[addonId] as? T?
    }

    /**
     * 获取条目进度
     * 并通过可能存在的 Stats 扩展
     */
    fun getProgress(profile: PlayerProfile): CompletableFuture<Progress> {
        return stats()?.getProgress(profile) ?: CompletableFuture.completedFuture(objective.getProgress(profile, this))
    }
}