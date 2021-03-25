package ink.ptms.chemdah.core.quest.addon

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.*
import org.bukkit.configuration.ConfigurationSection

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.addon.AddonTrack
 *
 * @author sky
 * @since 2021/3/11 9:05 上午
 */
@Id("track")
class AddonTrack(config: ConfigurationSection, questContainer: QuestContainer) : Addon(config, questContainer) {

    companion object {

        /**
         * 当前任务追踪
         */
        var PlayerProfile.trackQuest: Template?
            set(value) {
                if (value != null) {
                    persistentDataContainer["quest.track"] = value.id
                } else {
                    persistentDataContainer.remove("quest.track")
                }
            }
            get() = persistentDataContainer["quest.track"]?.let { getQuestById(it.toString())?.template }
    }
}