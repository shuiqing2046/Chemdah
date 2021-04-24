package ink.ptms.chemdah.core.quest.addon

import ink.ptms.chemdah.core.quest.Id
import ink.ptms.chemdah.core.quest.QuestContainer
import ink.ptms.chemdah.core.quest.Template
import org.bukkit.configuration.ConfigurationSection

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.addon.AddonParty
 *
 * @author sky
 * @since 2021/3/11 9:05 上午
 */
@Id("party")
class AddonParty(config: ConfigurationSection, questContainer: QuestContainer) : Addon(config, questContainer) {

    /**
     * 是否分享这个任务
     */
    val share = config.getBoolean("share")

    /**
     * 是否只有队长可以给队员共享这个任务
     */
    val shareOnlyLeader = config.getBoolean("share-only-leader")

    /**
     * 队员是否可以替你完成这个条目
     */
    val canContinue = config.getBoolean("continue")

    companion object {

        fun QuestContainer.party() = addon<AddonParty>("party")
    }
}