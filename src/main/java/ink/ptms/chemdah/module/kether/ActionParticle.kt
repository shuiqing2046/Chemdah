package ink.ptms.chemdah.module.kether

import ink.ptms.chemdah.util.Effect
import ink.ptms.chemdah.util.getPlayer
import io.izzel.taboolib.kotlin.kether.Kether.expects
import io.izzel.taboolib.kotlin.kether.KetherParser
import io.izzel.taboolib.kotlin.kether.ScriptParser
import io.izzel.taboolib.kotlin.kether.common.api.ParsedAction
import io.izzel.taboolib.kotlin.kether.common.api.QuestAction
import io.izzel.taboolib.kotlin.kether.common.api.QuestContext
import io.izzel.taboolib.kotlin.kether.common.loader.types.ArgTypes
import org.bukkit.Location
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.ActionParticle
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class ActionParticle {

    class ParticleNormal(val effect: Effect, val location: ParsedAction<*>, val self: Boolean = false) : QuestAction<Void>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<Void> {
            return frame.newFrame(location).run<Location>().thenAccept {
                if (self) {
                    effect.run(it, frame.getPlayer())
                } else {
                    effect.run(it)
                }
            }
        }

        override fun toString(): String {
            return "ParticleNormal(effect=$effect, location=$location, self=$self)"
        }

    }

    companion object {

        /**
         * particle normal "flame 0 0 0 -speed 0.1 -count 100" at location *world *0 *0 *0 @self
         */
        @KetherParser(["particle"])
        fun parser() = ScriptParser.parser {
            when (it.expects("normal")) {
                "normal" -> {
                    ParticleNormal(
                        Effect(it.nextToken()),
                        it.run {
                            it.expects("at", "on", "to")
                            it.next(ArgTypes.ACTION)
                        }, try {
                            it.mark()
                            it.expect("@self")
                            true
                        } catch (ex: Exception) {
                            it.reset()
                            false
                        }
                    )
                }
                else -> error("out of case")
            }
        }
    }
}