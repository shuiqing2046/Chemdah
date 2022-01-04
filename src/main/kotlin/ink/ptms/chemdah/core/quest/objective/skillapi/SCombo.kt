package ink.ptms.chemdah.core.quest.objective.skillapi

import com.sucy.skill.api.event.PlayerComboFinishEvent
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objiective.skillapi.SCombo
 *
 * @author GalaxyVN
 * @since 2021/7/18 2:05 下午
 */

@Dependency("SkillAPI")
object SCombo : ObjectiveCountableI<PlayerComboFinishEvent>() {

    override val name = "skillapi combo"
    override val event = PlayerComboFinishEvent::class.java

    init {
        handler {
            it.player
        }
        addSimpleCondition("position") { data, it ->
            data.toPosition().inside(it.player.location)
        }
        addSimpleCondition("skill") { data, it ->
            data.toString().equals(it.skill.name, true)
        }
        addSimpleCondition("combo") { data, it ->
            data.toInt() <= it.combo
        }
        addConditionVariable("combo") {
            it.combo
        }
    }

}