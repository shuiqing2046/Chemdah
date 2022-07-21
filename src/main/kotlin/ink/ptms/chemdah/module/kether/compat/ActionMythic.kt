package ink.ptms.chemdah.module.kether.compat

import ink.ptms.chemdah.util.getPlayer
import ink.ptms.um.Mythic
import ink.ptms.um.Skill
import taboolib.common.platform.function.submit
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture


/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.compat.ActionMythicMobs
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class ActionMythic {

    class MythicMobsCast(val mechanic: Skill, val trigger: Skill.Trigger) : ScriptAction<Void>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Void> {
            val player = frame.getPlayer()
            submit { mechanic.execute(trigger, player, player, emptySet(), emptySet(), 0f, emptyMap()) }
            return CompletableFuture.completedFuture(null);
        }
    }

    companion object {

        /**
         * mm cast skill_name
         * mm cast skill_name with api
         */
        @KetherParser(["mythicmobs", "mm"], shared = true)
        fun parser() = scriptParser {
            when (it.expects("cast")) {
                "cast" -> {
                    val skill = it.nextToken()
                    val mechanic = Mythic.API.getSkillMechanic(skill) ?: error("unknown skill $skill")
                    val trigger = try {
                        it.mark()
                        it.expects("with", "as", "by")
                        Mythic.API.getSkillTrigger(it.nextToken())
                    } catch (ex: Throwable) {
                        it.reset()
                        Mythic.API.getSkillTrigger("DEFAULT")
                    }
                    MythicMobsCast(mechanic, trigger)
                }
                else -> error("out of case")
            }
        }
    }
}