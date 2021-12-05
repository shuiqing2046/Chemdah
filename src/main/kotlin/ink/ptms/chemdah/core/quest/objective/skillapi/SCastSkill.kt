package ink.ptms.chemdah.core.quest.objective.skillapi

import com.sucy.skill.api.event.PlayerCastSkillEvent
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objiective.skillapi.SCastSkill
 *
 * @author Peng_Lx
 * @since 2021/5/29 7:59 下午
 */
@Dependency("SkillAPI")
object SCastSkill : ObjectiveCountableI<PlayerCastSkillEvent>() {

    override val name = "skillapi castskill"
    override val event = PlayerCastSkillEvent::class

    init {
        handler {
            player
        }
        addSimpleCondition("position") {
            toPosition().inside(it.player.location)
        }
        addSimpleCondition("skill") {
            toString().equals(it.skill.status.name, true)
        }
        addSimpleCondition("mana") {
            toDouble() <= it.skill.manaCost
        }
        addConditionVariable("mana") {
            it.skill.manaCost
        }
    }
}