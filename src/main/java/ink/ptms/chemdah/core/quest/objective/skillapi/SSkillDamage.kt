package ink.ptms.chemdah.core.quest.objective.skillapi

import ink.ptms.chemdah.core.quest.objective.Dependency
import com.sucy.skill.api.event.SkillDamageEvent
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.entity.Player

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
    override val event = SkillDamageEvent::class

    init {
        handler {
            damager as? Player
        }
        addCondition("damage") {
            toDouble() == it.damage
        }
        addCondition("damage more than") {
            toDouble() >= it.damage
        }
        addCondition("damage less than") {
            toDouble() <= it.damage
        }
        addCondition("invisible") {
            toBoolean() == it.damager.isInvisible
        }
        addCondition("jumping") {
            toBoolean() == it.damager.isJumping
        }
        addCondition("collidable") {
            toBoolean() == it.damager.isCollidable
        }
        addCondition("gliding") {
            toBoolean() == it.damager.isGliding
        }
        addCondition("hand raised") {
            toBoolean() == it.damager.isHandRaised
        }
        addCondition("leashed") {
            toBoolean() == it.damager.isLeashed
        }
        addCondition("sleeping") {
            toBoolean() == it.damager.isSleeping
        }
        addCondition("riptiding") {
            toBoolean() == it.damager.isRiptiding
        }
        addCondition("swimming") {
            toBoolean() == it.damager.isSwimming
        }
        addCondition("glowing") {
            toBoolean() == it.damager.isGlowing
        }
        addCondition("target invisible") {
            toBoolean() == it.target.isInvisible
        }
        addCondition("target jumping") {
            toBoolean() == it.target.isJumping
        }
        addCondition("target collidable") {
            toBoolean() == it.target.isCollidable
        }
        addCondition("target gliding") {
            toBoolean() == it.target.isGliding
        }
        addCondition("target hand raised") {
            toBoolean() == it.target.isHandRaised
        }
        addCondition("target leashed") {
            toBoolean() == it.target.isLeashed
        }
        addCondition("target sleeping") {
            toBoolean() == it.target.isSleeping
        }
        addCondition("target riptiding") {
            toBoolean() == it.target.isRiptiding
        }
        addCondition("target swimming") {
            toBoolean() == it.target.isSwimming
        }
        addCondition("target glowing") {
            toBoolean() == it.target.isGlowing
        }
    }
}