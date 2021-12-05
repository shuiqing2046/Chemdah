package ink.ptms.chemdah.core.quest.objective.skillapi

import com.sucy.skill.api.event.PlayerClassChangeEvent
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objiective.skillapi.SClassChange
 *
 * @author GalaxyVN
 * @since 2021/7/18 2:05 下午
 */

@Dependency("SkillAPI")
object SClassChange : ObjectiveCountableI<PlayerClassChangeEvent>() {

    override val name = "skillapi class change"
    override val event = PlayerClassChangeEvent::class

    init {
        handler {
            playerData.player
        }
        addSimpleCondition("position") {
            toPosition().inside(it.playerData.player.location)
        }
        addSimpleCondition("class") {
            toString().equals(it.newClass.name, true)
        }
        addConditionVariable("class") {
            it.newClass.name
        }
    }
}