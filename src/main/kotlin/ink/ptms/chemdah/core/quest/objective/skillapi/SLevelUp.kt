package ink.ptms.chemdah.core.quest.objective.skillapi

import com.sucy.skill.api.event.PlayerLevelUpEvent
import ink.ptms.chemdah.core.quest.objective.Dependency
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

    override val name = "skillapi levelup"
    override val event = PlayerLevelUpEvent::class.java

    init {
        handler {
            it.playerData.player
        }
        addSimpleCondition("position") { data, it ->
            data.toPosition().inside(it.playerData.player.location)
        }
        addSimpleCondition("level") { data, it ->
            data.toInt() <= it.level
        }
        addConditionVariable("level") {
            it.level
        }
    }
}