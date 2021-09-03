package ink.ptms.chemdah.module.kether

import ink.ptms.chemdah.util.Effect
import ink.ptms.chemdah.util.getPlayer
import taboolib.module.kether.*
import taboolib.library.kether.ParsedAction
import org.bukkit.Location
import taboolib.library.kether.ArgTypes
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.ActionParticle
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class ActionParticle {

    class ParticleNormal(val effect: Effect, val location: ParsedAction<*>, val self: Boolean = false) : ScriptAction<Void>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Void> {
            return frame.newFrame(location).run<Location>().thenAccept {
                if (self) {
                    effect.run(it, frame.getPlayer())
                } else {
                    effect.run(it)
                }
            }
        }
    }

    companion object {

        /**
         * particle normal "flame 0 0 0 -speed 0.1 -count 100" at location *world *0 *0 *0 @self
         */
        @KetherParser(["particle"], shared = true)
        fun parser() = scriptParser {
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