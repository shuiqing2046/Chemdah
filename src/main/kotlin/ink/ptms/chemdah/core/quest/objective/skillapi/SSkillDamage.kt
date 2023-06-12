package ink.ptms.chemdah.core.quest.objective.skillapi

import com.sucy.skill.api.event.SkillDamageEvent
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.entity.Player
import taboolib.common5.cdouble

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objiective.skillapi.SSkillDamage
 *
 * @author Peng_Lx
 * @since 2021/5/29 7:59 下午
 */
@Dependency("SkillAPI")
object SSkillDamage : ObjectiveCountableI<SkillDamageEvent>() {

    override val name = "skillapi skill damage"
    override val event = SkillDamageEvent::class.java

    init {
        handler {
            it.damager as? Player
        }
        addSimpleCondition("position") { data, it ->
            data.toPosition().inside(it.damager.location)
        }
        addSimpleCondition("damage") { data, it ->
            data.toDouble() <= it.damage
        }
        addConditionVariable("damage") {
            it.damage
        }
        addConditionVariable("target") {
            it.target
        }
    }
}