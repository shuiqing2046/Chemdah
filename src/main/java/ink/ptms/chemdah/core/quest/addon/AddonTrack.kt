package ink.ptms.chemdah.core.quest.addon

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Id
import ink.ptms.chemdah.core.quest.QuestContainer
import ink.ptms.chemdah.core.quest.Template
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
         * 任务允许被追踪
         */
        fun Template.isTrackable() = addon<AddonTrack>("track") != null

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
            get() = persistentDataContainer["quest.track"]?.let { ChemdahAPI.getQuestTemplate(it.toString()) }
    }
}