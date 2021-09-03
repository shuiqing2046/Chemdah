package ink.ptms.chemdah.core.quest.objective.skillapi

import com.sucy.skill.api.event.PlayerSkillDowngradeEvent
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objiective.skillapi.SSkillDowngrade
 *
 * @author Peng_Lx
 * @since 2021/5/29 7:59 下午
 */
@Dependency("SkillAPI")

object SSkillDowngrade : ObjectiveCountableI<PlayerSkillDowngradeEvent>() {

    override val name = "skillapi skill downgrade"
    override val event = PlayerSkillDowngradeEvent::class

    init {
        handler {
            playerData.player
        }
        addCondition("position") {
            toPosition().inside(it.playerData.player.location)
        }
        addCondition("skill") {
            toString().equals(it.downgradedSkill.status.name, true)
        }
        addConditionVariable("skill") {
            it.downgradedSkill.status.name
        }
    }
}