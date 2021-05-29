package ink.ptms.chemdah.core.quest.objective.skillapi

import com.sucy.skill.api.event.PlayerCastSkillEvent
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import ink.ptms.chemdah.core.quest.objective.Dependency

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
        addCondition("skill") {
            toString().equals(it.skill.status.name, true)
        }
        addCondition("mana") {
            toDouble()  == it.skill.manaCost
        }
        addCondition("mana more than") {
            toDouble() >= it.skill.manaCost
        }
        addCondition("mana less than") {
            toDouble() <= it.skill.manaCost
        }
    }
}