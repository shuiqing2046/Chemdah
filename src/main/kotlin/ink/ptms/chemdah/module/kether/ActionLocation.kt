package ink.ptms.chemdah.module.kether

import org.bukkit.Location
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.*

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.ActionLocation
 *
 * @author sky
 * @since 2022/9/20 2:00 下午
 */
object ActionLocation {

    @KetherParser(["x"], shared = true)
    fun parserX() = scriptParser { buildAction(it, { loc -> loc.x }, { loc, v -> loc.x = v }, { loc, v -> loc.x += v }) }

    @KetherParser(["y"], shared = true)
    fun parserY() = scriptParser { buildAction(it, { loc -> loc.y }, { loc, v -> loc.y = v }, { loc, v -> loc.y += v }) }

    @KetherParser(["z"], shared = true)
    fun parserZ() = scriptParser { buildAction(it, { loc -> loc.z }, { loc, v -> loc.z = v }, { loc, v -> loc.z += v }) }

    @KetherParser(["yaw"], shared = true)
    fun parserYaw() = scriptParser { buildAction(it, { loc -> loc.yaw }, { loc, v -> loc.yaw = v.toFloat() }, { loc, v -> loc.yaw += v.toFloat() }) }

    @KetherParser(["pitch"], shared = true)
    fun parserPitch() = scriptParser { buildAction(it, { loc -> loc.pitch }, { loc, v -> loc.pitch = v.toFloat() }, { loc, v -> loc.pitch += v.toFloat() }) }

    @KetherParser(["block"], shared = true)
    fun parserBlock() = scriptParser {
        val type = it.expects("x", "y", "z")
        it.expect("in")
        val input = it.nextParsedAction()
        when (type) {
            "x" -> actionFuture { f -> newFrame(input).run<Location>().thenApply { loc -> f.complete(loc.blockX) } }
            "y" -> actionFuture { f -> newFrame(input).run<Location>().thenApply { loc -> f.complete(loc.blockY) } }
            "z" -> actionFuture { f -> newFrame(input).run<Location>().thenApply { loc -> f.complete(loc.blockZ) } }
            else -> error("out of case")
        }
    }

    @KetherParser(["distance"], shared = true)
    fun parserDistance() = scriptParser {
        val loc1 = it.next(ArgTypes.ACTION)
        it.expects("and", "to")
        val loc2 = it.next(ArgTypes.ACTION)
        actionFuture { f ->
            newFrame(loc1).run<Location>().thenApply { loc1 ->
                newFrame(loc2).run<Location>().thenApply { loc2 ->
                    f.complete(if (loc1.world == loc2.world) loc1.distance(loc2) else -1.0)
                }
            }
        }
    }

    fun buildAction(it: QuestReader, read: (Location) -> Any, write: (Location, Double) -> Unit, append: (Location, Double) -> Unit): ScriptAction<Any?> {
        it.expect("in")
        val input = it.nextParsedAction()
        return try {
            it.mark()
            when (it.expects("=", "+", "to", "add", "increase")) {
                "=", "to" -> {
                    val value = it.nextParsedAction()
                    actionFuture { f ->
                        newFrame(input).run<Location>().thenApply { loc ->
                            run(value).double { value ->
                                write(loc, value)
                                f.complete(loc)
                            }
                        }
                    }
                }

                "+", "add", "increase" -> {
                    val value = it.nextParsedAction()
                    actionFuture { f ->
                        newFrame(input).run<Location>().thenApply { loc ->
                            run(value).double { value ->
                                append(loc, value)
                                f.complete(loc)
                            }
                        }
                    }
                }

                else -> error("out of case")
            }
        } catch (ex: Exception) {
            it.reset()
            actionFuture { f -> newFrame(input).run<Location>().thenApply { loc -> f.complete(read(loc)) } }
        }
    }
}