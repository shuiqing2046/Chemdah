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

    /**
     * 持续在 UI 中显示
     * 启用后则可能显示为 can-start 或 cannot-start
     */
    val visibleStart = config.getBoolean("visible.start", false)

    /**
     * 任务完成后显示为 complete 已完成状态
     */
    val visibleComplete = config.getBoolean("visible.complete", true)

    /**
     * 显示图标
     */
    val icon = config.getString("icon")

    /**
     * 显示介绍
     */
    val description = config.getStringList("description").toList()

    companion object {

        fun Template.ui() = addon<AddonUI>("ui")
    }
}