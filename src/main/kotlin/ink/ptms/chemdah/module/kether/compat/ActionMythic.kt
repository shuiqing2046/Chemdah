package ink.ptms.chemdah.module.kether.compat

import com.google.common.collect.Sets
import ink.ptms.chemdah.util.getPlayer
import io.lumine.xikage.mythicmobs.MythicMobs
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitPlayer
import io.lumine.xikage.mythicmobs.mobs.GenericCaster
import io.lumine.xikage.mythicmobs.skills.SkillMechanic
import io.lumine.xikage.mythicmobs.skills.SkillMetadata
import io.lumine.xikage.mythicmobs.skills.SkillTrigger
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

    class MythicMobsCast(val mechanic: SkillMechanic, val trigger: SkillTrigger) : ScriptAction<Void>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Void> {
            submit {
                val bukkitPlayer = BukkitPlayer(frame.getPlayer())
                MythicMobs.inst().skillManager.runSecondPass()
                mechanic.executeSkills(
                    SkillMetadata(
                        trigger,
                        GenericCaster(bukkitPlayer),
                        bukkitPlayer,
                        BukkitAdapter.adapt(bukkitPlayer.entityAsPlayer.location),
                        Sets.newHashSet(),
                        Sets.newHashSet(),
                        0f
                    )
                )
            }
            return CompletableFuture.completedFuture(null);
        }
    }

    companion object {

        private val triggers by lazy {
            SkillTrigger.values().map { trigger -> trigger.name.lowercase() }.toTypedArray()
        }

        /**
         * mm cast skill_name
         * mm cast skill_name with api
         */
        @KetherParser(["mythicmobs", "mm"], shared = true)
        fun parser() = scriptParser {
            when (it.expects("cast")) {
                "cast" -> {
                    val mechanic = MythicMobs.inst().skillManager.getSkillMechanic(it.nextToken())
                    val trigger = try {
                        it.mark()
                        it.expects("with", "as", "by")
                        SkillTrigger.valueOf(it.expects(*triggers).uppercase())
                    } catch (ex: Throwable) {
                        it.reset()
                        SkillTrigger.DEFAULT
                    }
                    MythicMobsCast(mechanic, trigger)
                }
                else -> error("out of case")
            }
        }
    }
}