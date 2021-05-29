package ink.ptms.chemdah.core.quest.objective.skillapi

import ink.ptms.chemdah.core.quest.objective.Dependency
import com.sucy.skill.api.event.PlayerUpAttributeEvent
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objiective.skillapi.SUpAttribute
 *
 * @author Peng_Lx
 * @since 2021/5/29 7:59 下午
 */
@Dependency("SkillAPI")

object SUpAttribute : ObjectiveCountableI<PlayerUpAttributeEvent>() {

    override val name = "skillapi attr up"
    override val event = PlayerUpAttributeEvent::class

    init {
        handler {
            playerData.player
        }
        addCondition("attr") {
            toString().equals(it.attribute, true)
        }
    }
}