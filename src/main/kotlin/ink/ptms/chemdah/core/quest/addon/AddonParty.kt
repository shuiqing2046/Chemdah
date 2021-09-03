package ink.ptms.chemdah.core.quest.addon

import ink.ptms.chemdah.core.quest.Id
import ink.ptms.chemdah.core.quest.Option
import ink.ptms.chemdah.core.quest.QuestContainer
import taboolib.library.configuration.ConfigurationSection

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.addon.AddonParty
 *
 * @author sky
 * @since 2021/3/11 9:05 上午
 */
@Id("party")
@Option(Option.Type.SECTION)
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
     * 队员是否可以替你进行这个条目
     */
    val canContinue = config.getBoolean("continue")

    /**
     * 只有处于队伍中，且队员人数达到需求才能进行该任务或条目
     * 任务和条目均支持该配置
     */
    val requireMembers = config.getInt("require-members")

    companion object {

        fun QuestContainer.party() = addon<AddonParty>("party")
    }
}