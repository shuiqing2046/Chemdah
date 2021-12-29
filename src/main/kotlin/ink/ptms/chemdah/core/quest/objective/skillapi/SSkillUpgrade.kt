package ink.ptms.chemdah.core.quest.objective.skillapi

import com.sucy.skill.api.event.PlayerSkillUpgradeEvent
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objiective.skillapi.SSkillUpgrade
 *
 * @author Peng_Lx
 * @since 2021/5/29 7:59 下午
 */
@Dependency("SkillAPI")

object SSkillUpgrade : ObjectiveCountableI<PlayerSkillUpgradeEvent>() {

    override val name = "skillapi skill upgrade"
    override val event = PlayerSkillUpgradeEvent::class.java

    init {
        handler {
            playerData.player
        }
        addSimpleCondition("position") {
            toPosition().inside(it.playerData.player.location)
        }
        addSimpleCondition("skill") {
            toString().equals(it.upgradedSkill.status.name, true)
        }
        addConditionVariable("skill") {
            it.upgradedSkill.status.name
        }
    }
}