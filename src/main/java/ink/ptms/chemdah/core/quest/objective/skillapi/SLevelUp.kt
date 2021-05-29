package ink.ptms.chemdah.core.quest.objective.skillapi

import ink.ptms.chemdah.core.quest.objective.Dependency
import com.sucy.skill.api.event.PlayerLevelUpEvent
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objiective.skillapi.SLevelUp
 *
 * @author Peng_Lx
 * @since 2021/5/29 7:59 下午
 */
@Dependency("SkillAPI")
object SLevelUp : ObjectiveCountableI<PlayerLevelUpEvent>() {

    override val name = "skillapi level up"
    override val event = PlayerLevelUpEvent::class

    init {
        handler {
            playerData.player
        }
        addCondition("level") {
            toInt() == it.level
        }
        addCondition("level more than") {
            toInt() >= it.level
        }
        addCondition("level less than") {
            toInt() <= it.level
        }
    }
}