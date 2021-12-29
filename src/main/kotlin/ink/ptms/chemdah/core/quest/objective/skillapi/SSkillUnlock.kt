package ink.ptms.chemdah.core.quest.objective.skillapi

import com.sucy.skill.api.event.PlayerSkillUnlockEvent
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objiective.skillapi.SSkillUnlock
 *
 * @author Peng_Lx
 * @since 2021/5/29 7:59 下午
 */
@Dependency("SkillAPI")
object SSkillUnlock : ObjectiveCountableI<PlayerSkillUnlockEvent>() {

    override val name = "skillapi skillunlock"
    override val event = PlayerSkillUnlockEvent::class.java

    init {
        handler {
            playerData.player
        }
        addSimpleCondition("position") {
            toPosition().inside(it.playerData.player.location)
        }
        addSimpleCondition("skill") {
            toString().equals(it.unlockedSkill.status.name, true)
        }
        addConditionVariable("skill") {
            it.unlockedSkill.status.name
        }
    }
}