package ink.ptms.chemdah.module.kether

import org.bukkit.Location
import taboolib.common5.Coerce
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.ConversationLocation
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
@Suppress("DuplicatedCode")
class ActionLocation {

    class LocationFunc(val location: ParsedAction<*>, val value: ParsedAction<*>, val func: (Location, Any) -> Any) : ScriptAction<Any>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Any> {
            val future = CompletableFuture<Any>()
            frame.newFrame(location).run<Location>().thenApply {
                frame.newFrame(value).run<Any>().thenApply { value ->
                    future.complete(func(it, value))
                }
            }
            return future
        }

        override fun toString(): String {
            return "LocationFunc(location=$location, value=$value, func=$func)"
        }
    }

    class LocationDistance(val loc1: ParsedAction<*>, val loc2: ParsedAction<*>) : ScriptAction<Double>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Double> {
            val future = CompletableFuture<Double>()
            frame.newFrame(loc1).run<Location>().thenApply { loc1 ->
                frame.newFrame(loc2).run<Location>().thenApply { loc2 ->
                    future.complete(if (loc1.world == loc2.world) loc1.distance(loc2) else -1.0)
                }
            }
            return future
        }

        override fun toString(): String {
            return "LocationDistance(loc1=$loc1, loc2=$loc2)"
        }

    }

    companion object {

        @KetherParser(["x"], shared = true)
        fun parserX() = scriptParser {
            it.expect("in")
            val input = it.next(ArgTypes.ACTION)
            try {
                it.mark()
                when (it.expects("=","+", "to", "add", "increase")) {
                    "=", "to" -> LocationFunc(input, it.next(ArgTypes.ACTION)) { loc, value ->
                        loc.x = Coerce.toDouble(value)
                        loc
                    }
                    "+", "add", "increase" -> LocationFunc(input, it.next(ArgTypes.ACTION)) { loc, value ->
                        loc.x += Coerce.toDouble(value)
                        loc.x
                    }
                    else -> error("out of case")
                }
            } catch (ex: Exception) {
                it.reset()
                LocationFunc(input, ParsedAction.noop<Any>()) { loc, _ ->
                    loc.x
                }
            }
        }

        @KetherParser(["y"], shared = true)
        fun parserY() = scriptParser {
            it.expect("in")
            val input = it.next(ArgTypes.ACTION)
            try {
                it.mark()
                when (it.expects("=", "+", "to", "add", "increase")) {
                    "=", "to" -> LocationFunc(input, it.next(ArgTypes.ACTION)) { loc, value ->
                        loc.y = Coerce.toDouble(value)
                        loc
                    }
                    "+", "add", "increase" -> LocationFunc(input, it.next(ArgTypes.ACTION)) { loc, value ->
                        loc.y += Coerce.toDouble(value)
                        loc
                    }
                    else -> error("out of case")
                }
            } catch (ex: Exception) {
                it.reset()
                LocationFunc(input, ParsedAction.noop<Any>()) { loc, _ -> loc.y }
            }
        }

        @KetherParser(["z"], shared = true)
        fun parserZ() = scriptParser {
            it.expect("in")
            val input = it.next(ArgTypes.ACTION)
            try {
                it.mark()
                when (it.expects("=", "+", "to", "add", "increase")) {
                    "=", "to" -> LocationFunc(input, it.next(ArgTypes.ACTION)) { loc, value ->
                        loc.z = Coerce.toDouble(value)
                        loc
                    }
                    "+", "add", "increase" -> LocationFunc(input, it.next(ArgTypes.ACTION)) { loc, value ->
                        loc.z += Coerce.toDouble(value)
                        loc
                    }
                    else -> error("out of case")
                }
            } catch (ex: Exception) {
                it.reset()
                LocationFunc(input, ParsedAction.noop<Any>()) { loc, _ -> loc.z }
            }
        }

        @KetherParser(["yaw"], shared = true)
        fun parserYaw() = scriptParser {
            it.expect("in")
            val input = it.next(ArgTypes.ACTION)
            try {
                it.mark()
                when (it.expects("=", "+", "to", "add", "increase")) {
                    "=", "to" -> LocationFunc(input, it.next(ArgTypes.ACTION)) { loc, value ->
                        loc.yaw = Coerce.toFloat(value)
                        loc
                    }
                    "+", "add", "increase" -> LocationFunc(input, it.next(ArgTypes.ACTION)) { loc, value ->
                        loc.yaw += Coerce.toFloat(value)
                        loc
                    }
                    else -> error("out of case")
                }
            } catch (ex: Exception) {
                it.reset()
                LocationFunc(input, ParsedAction.noop<Any>()) { loc, _ -> loc.yaw }
            }
        }

        @KetherParser(["pitch"], shared = true)
        fun parserPitch() = scriptParser {
            val input = it.next(ArgTypes.ACTION)
            try {
                it.mark()
                when (it.expects("+", "=", "to", "add", "increase")) {
                    "=", "to" -> LocationFunc(input, it.next(ArgTypes.ACTION)) { loc, value ->
                        loc.pitch = Coerce.toFloat(value)
                        loc
                    }
                    "+", "add", "increase" -> LocationFunc(input, it.next(ArgTypes.ACTION)) { loc, value ->
                        loc.pitch += Coerce.toFloat(value)
                        loc
                    }
                    else -> error("out of case")
                }
            } catch (ex: Exception) {
                it.reset()
                LocationFunc(input, ParsedAction.noop<Any>()) { loc, _ -> loc.pitch }
            }
        }

        @KetherParser(["block"], shared = true)
        fun parserBlock() = scriptParser {
            val type = it.expects("x", "y", "z")
            it.expect("in")
            when (type) {
                "x" -> LocationFunc(it.next(ArgTypes.ACTION), ParsedAction.noop<Any>()) { loc, _ ->
                    loc.blockX
                }
                "y" -> LocationFunc(it.next(ArgTypes.ACTION), ParsedAction.noop<Any>()) { loc, _ ->
                    loc.blockY
                }
                "z" -> LocationFunc(it.next(ArgTypes.ACTION), ParsedAction.noop<Any>()) { loc, _ ->
                    loc.blockZ
                }
                else -> error("out of case")
            }
        }

        @KetherParser(["distance"], shared = true)
        fun parserDistance() = scriptParser {
            val loc1 = it.next(ArgTypes.ACTION)
            it.expects("and", "to")
            val loc2 = it.next(ArgTypes.ACTION)
            LocationDistance(loc1, loc2)
        }
    }
}