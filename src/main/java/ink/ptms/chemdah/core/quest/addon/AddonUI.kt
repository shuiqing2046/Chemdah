package ink.ptms.chemdah.core.quest.addon

import ink.ptms.chemdah.core.quest.Id
import ink.ptms.chemdah.core.quest.QuestContainer
import ink.ptms.chemdah.core.quest.Template
import org.bukkit.configuration.ConfigurationSection

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.addon.AddonUI
 *
 * @author sky
 * @since 2021/3/11 9:05 上午
 */
@Id("ui")
class AddonUI(config: ConfigurationSection, questContainer: QuestContainer) : Addon(config, questContainer) {

    val visible = config.getBoolean("visible")
    val start = config.getBoolean("start")
    val icon = config.getString("icon")
    val description: List<String> = config.getStringList("description")

    companion object {

        fun Template.ui() = addon<AddonUI>("ui")
    }
}