package ink.ptms.chemdah.module.kether.conversation

import ink.ptms.chemdah.util.getSession
import io.izzel.taboolib.kotlin.kether.Kether.expects
import io.izzel.taboolib.kotlin.kether.KetherParser
import io.izzel.taboolib.kotlin.kether.ScriptParser
import io.izzel.taboolib.kotlin.kether.common.api.ParsedAction
import io.izzel.taboolib.kotlin.kether.common.api.QuestAction
import io.izzel.taboolib.kotlin.kether.common.api.QuestContext
import io.izzel.taboolib.kotlin.kether.common.loader.types.ArgTypes
import io.izzel.taboolib.util.Coerce
import org.bukkit.Location
import java.lang.Exception
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.conversation.ConversationLocation
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class ConversationLocation {

    class LocationOrigin : QuestAction<Location>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<Location> {
            return CompletableFuture.completedFuture(frame.getSession().origin)
        }
    }

    class LocationFunc(val location: ParsedAction<*>, val value: ParsedAction<*>, val func: (Location, Any) -> Any) : QuestAction<Any>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<Any> {
            return frame.newFrame(location).run<Location>().thenApply { location ->
                frame.newFrame(value).run<Any>().thenApply { value ->
                    func(location, value)
                }
            }
        }
    }

    companion object {

        @KetherParser(["x"])
        fun parserX() = ScriptParser.parser {
            it.expect("in")
            val input = it.next(ArgTypes.ACTION)
            try {
                it.mark()
                when (it.expects("to", "add", "increase")) {
                    "to" -> LocationFunc(input, it.next(ArgTypes.ACTION)) { loc, value ->
                        loc.x = Coerce.toDouble(value)
                        loc
                    }
                    "add", "increase" -> LocationFunc(input, it.next(ArgTypes.ACTION)) { loc, value ->
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

        @KetherParser(["y"])
        fun parserY() = ScriptParser.parser {
            it.expect("in")
            val input = it.next(ArgTypes.ACTION)
            try {
                it.mark()
                when (it.expects("to", "add", "increase")) {
                    "to" -> LocationFunc(input, it.next(ArgTypes.ACTION)) { loc, value ->
                        loc.y = Coerce.toDouble(value)
                        loc
                    }
                    "add", "increase" -> LocationFunc(input, it.next(ArgTypes.ACTION)) { loc, value ->
                        loc.y += Coerce.toDouble(value)
                        loc
                    }
                    else -> error("out of case")
                }
            } catch (ex: Exception) {
                it.reset()
                LocationFunc(input, ParsedAction.noop<Any>()) { loc, _ ->
                    loc.y
                }
            }
        }

        @KetherParser(["z"])
        fun parserZ() = ScriptParser.parser {
            it.expect("in")
            val input = it.next(ArgTypes.ACTION)
            try {
                it.mark()
                when (it.expects("to", "add", "increase")) {
                    "to" -> LocationFunc(input, it.next(ArgTypes.ACTION)) { loc, value ->
                        loc.z = Coerce.toDouble(value)
                        loc
                    }
                    "add", "increase" -> LocationFunc(input, it.next(ArgTypes.ACTION)) { loc, value ->
                        loc.z += Coerce.toDouble(value)
                        loc
                    }
                    else -> error("out of case")
                }
            } catch (ex: Exception) {
                it.reset()
                LocationFunc(input, ParsedAction.noop<Any>()) { loc, _ ->
                    loc.z
                }
            }
        }

        @KetherParser(["yaw"])
        fun parserYaw() = ScriptParser.parser {
            it.expect("in")
            val input = it.next(ArgTypes.ACTION)
            try {
                it.mark()
                when (it.expects("to", "add", "increase")) {
                    "to" -> LocationFunc(input, it.next(ArgTypes.ACTION)) { loc, value ->
                        loc.yaw = Coerce.toFloat(value)
                        loc
                    }
                    "add", "increase" -> LocationFunc(input, it.next(ArgTypes.ACTION)) { loc, value ->
                        loc.yaw += Coerce.toFloat(value)
                        loc
                    }
                    else -> error("out of case")
                }
            } catch (ex: Exception) {
                it.reset()
                LocationFunc(input, ParsedAction.noop<Any>()) { loc, _ ->
                    loc.yaw
                }
            }
        }

        @KetherParser(["pitch"])
        fun parserPitch() = ScriptParser.parser {
            val input = it.next(ArgTypes.ACTION)
            try {
                it.mark()
                when (it.expects("to", "add", "increase")) {
                    "to" -> LocationFunc(input, it.next(ArgTypes.ACTION)) { loc, value ->
                        loc.pitch = Coerce.toFloat(value)
                        loc
                    }
                    "add", "increase" -> LocationFunc(input, it.next(ArgTypes.ACTION)) { loc, value ->
                        loc.pitch += Coerce.toFloat(value)
                        loc
                    }
                    else -> error("out of case")
                }
            } catch (ex: Exception) {
                it.reset()
                LocationFunc(input, ParsedAction.noop<Any>()) { loc, _ ->
                    loc.pitch
                }
            }
        }

        @KetherParser(["block"])
        fun parserBlock() = ScriptParser.parser {
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

        @KetherParser(["origin"], namespace = "chemdah-conversation")
        fun parser() = ScriptParser.parser {
            LocationOrigin()
        }
    }
}