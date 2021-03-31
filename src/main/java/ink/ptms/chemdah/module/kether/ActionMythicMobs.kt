package ink.ptms.chemdah.module.kether

import io.izzel.taboolib.kotlin.kether.KetherParser
import io.izzel.taboolib.kotlin.kether.ScriptParser
import io.izzel.taboolib.kotlin.kether.common.api.QuestAction
import io.izzel.taboolib.kotlin.kether.common.api.QuestContext
import java.util.concurrent.CompletableFuture
import io.lumine.xikage.mythicmobs.skills.SkillTrigger

import io.lumine.xikage.mythicmobs.skills.SkillMetadata

import io.lumine.xikage.mythicmobs.mobs.GenericCaster

import io.lumine.xikage.mythicmobs.MythicMobs

import com.google.common.collect.Sets
import io.izzel.taboolib.kotlin.Tasks
import io.izzel.taboolib.kotlin.kether.Kether.expects
import io.izzel.taboolib.kotlin.kether.script

import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitPlayer
import io.lumine.xikage.mythicmobs.skills.SkillMechanic
import org.bukkit.entity.Player


/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.ActionMythicMobs
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class ActionMythicMobs {

    class MythicMobsCast(val mechanic: SkillMechanic, val trigger: SkillTrigger) : QuestAction<Void>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<Void> {
            Tasks.task {
                val bukkitPlayer = BukkitPlayer(frame.script().sender as? Player ?: error("No player selected."))
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

        private val triggers = SkillTrigger.values().map { trigger -> trigger.name.toLowerCase() }.toTypedArray()

        /**
         * mm cast skill_name
         * mm cast skill_name with api
         */
        @KetherParser(["mythicmobs", "mm"], namespace = "chemdah")
        fun parser() = ScriptParser.parser {
            when (it.expects("cast")) {
                "cast" -> {
                    val mechanic = MythicMobs.inst().skillManager.getSkillMechanic(it.nextToken())
                    val trigger = try {
                        it.mark()
                        it.expects("with", "as", "by")
                        SkillTrigger.valueOf(it.expects(*triggers).toUpperCase())
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