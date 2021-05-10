package ink.ptms.chemdah.module.kether

import ink.ptms.chemdah.module.level.LevelSystem
import ink.ptms.chemdah.module.level.LevelSystem.getLevel
import ink.ptms.chemdah.module.level.LevelSystem.setLevel
import ink.ptms.chemdah.util.getProfile
import ink.ptms.chemdah.util.increaseAny
import io.izzel.taboolib.kotlin.kether.Kether.expects
import io.izzel.taboolib.kotlin.kether.KetherParser
import io.izzel.taboolib.kotlin.kether.ScriptParser
import io.izzel.taboolib.kotlin.kether.action.bukkit.Symbol
import io.izzel.taboolib.kotlin.kether.common.api.ParsedAction
import io.izzel.taboolib.kotlin.kether.common.api.QuestAction
import io.izzel.taboolib.kotlin.kether.common.api.QuestContext
import io.izzel.taboolib.kotlin.kether.common.loader.types.ArgTypes
import io.izzel.taboolib.util.Coerce
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.ActionProfile
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class ActionProfile {

    class ProfileDataGet(val key: ParsedAction<*>, val default: ParsedAction<*> = ParsedAction.noop<Any>()) : QuestAction<Any>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<Any> {
            return frame.newFrame(key).run<Any>().thenApply {
                frame.newFrame(default).run<Any>().thenApply { def ->
                    frame.getProfile().persistentDataContainer[it.toString()]?.value ?: def
                }
            }
        }

        override fun toString(): String {
            return "ProfileDataGet(key=$key, default=$default)"
        }

    }

    class ProfileDataSet(
        val key: ParsedAction<*>,
        val value: ParsedAction<*>,
        val symbol: Symbol,
        val default: ParsedAction<*> = ParsedAction.noop<Any>()
    ) : QuestAction<Void>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<Void> {
            return frame.newFrame(key).run<Any>().thenAccept { key ->
                frame.newFrame(value).run<Any>().thenAccept { value ->
                    frame.newFrame(default).run<Any>().thenAccept { def ->
                        val persistentDataContainer = frame.getProfile().persistentDataContainer
                        when {
                            value == null -> {
                                persistentDataContainer.remove(key.toString())
                            }
                            symbol == Symbol.ADD -> {
                                persistentDataContainer[key.toString()] = (persistentDataContainer[key.toString()] ?: def).increaseAny(value)
                            }
                            else -> {
                                persistentDataContainer[key.toString()] = value
                            }
                        }
                    }
                }
            }
        }

        override fun toString(): String {
            return "ProfileDataSet(key=$key, value=$value, symbol=$symbol, default=$default)"
        }

    }

    class ProfileDataKeys : QuestAction<List<String>>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<List<String>> {
            return CompletableFuture.completedFuture(frame.getProfile().persistentDataContainer.keys())
        }

        override fun toString(): String {
            return "ProfileDataKeys()"
        }

    }

    class ProfileLevelSet(val key: ParsedAction<*>, val type: LevelType, val value: ParsedAction<*>, val symbol: Symbol) : QuestAction<Void>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<Void> {
            return frame.newFrame(key).run<Any>().thenAccept { key ->
                frame.newFrame(value).run<Any>().thenAccept { value ->
                    val option = LevelSystem.getLevelOption(key.toString())
                    if (option != null) {
                        val playerProfile = frame.getProfile()
                        val playerLevel = option.toLevel(playerProfile.getLevel(option))
                        if (symbol == Symbol.ADD) {
                            if (type == LevelType.LEVEL) {
                                playerLevel.addLevel(Coerce.toInteger(value)).thenAccept {
                                    playerProfile.setLevel(option, playerLevel.toPlayerLevel())
                                }
                            } else {
                                playerLevel.addExperience(Coerce.toInteger(value)).thenAccept {
                                    playerProfile.setLevel(option, playerLevel.toPlayerLevel())
                                }
                            }
                        } else {
                            if (type == LevelType.LEVEL) {
                                playerLevel.setLevel(Coerce.toInteger(value)).thenAccept {
                                    playerProfile.setLevel(option, playerLevel.toPlayerLevel())
                                }
                            } else {
                                playerLevel.setExperience(Coerce.toInteger(value)).thenAccept {
                                    playerProfile.setLevel(option, playerLevel.toPlayerLevel())
                                }
                            }
                        }
                    }
                }
            }
        }

        override fun toString(): String {
            return "ProfileLevelSet(key=$key, type=$type, value=$value, symbol=$symbol)"
        }

    }

    class ProfileLevelGet(val key: ParsedAction<*>, val type: LevelType) : QuestAction<Int>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<Int> {
            return frame.newFrame(key).run<Any>().thenApply {
                val option = LevelSystem.getLevelOption(it.toString())
                if (option != null) {
                    if (type == LevelType.LEVEL) {
                        frame.getProfile().getLevel(option).level
                    } else {
                        frame.getProfile().getLevel(option).experience
                    }
                } else {
                    -1
                }
            }
        }

        override fun toString(): String {
            return "ProfileLevelGet(key=$key, type=$type)"
        }

    }

    enum class LevelType {

        LEVEL, EXP
    }

    companion object {

        /**
         * profile data *key
         * profile data *key to *value
         * profile data *key add *value
         * profile data keys
         *
         * profile level *default level
         * profile level *default level to *100
         * profile level *default exp
         * profile level *default exp add *100
         */
        @KetherParser(["profile"])
        fun parser() = ScriptParser.parser {
            when (it.expects("data", "level")) {
                "data" -> {
                    try {
                        it.mark()
                        it.expect("keys")
                        ProfileDataKeys()
                    } catch (ex: Throwable) {
                        it.reset()
                        val key = it.next(ArgTypes.ACTION)
                        try {
                            it.mark()
                            when (it.expects("to", "add", "increase")) {
                                "to" -> ProfileDataSet(key, it.next(ArgTypes.ACTION), Symbol.SET)
                                "add", "increase" -> {
                                    val value = it.next(ArgTypes.ACTION)
                                    try {
                                        it.mark()
                                        it.expect("default")
                                        ProfileDataSet(key, value, Symbol.ADD, it.next(ArgTypes.ACTION))
                                    } catch (ex: Throwable) {
                                        it.reset()
                                        ProfileDataSet(key, value, Symbol.ADD)
                                    }
                                }
                                else -> error("out of case")
                            }
                        } catch (ex: Throwable) {
                            it.reset()
                            try {
                                it.mark()
                                it.expect("default")
                                ProfileDataGet(key, it.next(ArgTypes.ACTION))
                            } catch (ex: Throwable) {
                                it.reset()
                                ProfileDataGet(key)
                            }
                        }
                    }
                }
                "level" -> {
                    val key = it.next(ArgTypes.ACTION)
                    val type = when (it.expects("level", "exp")) {
                        "level" -> LevelType.LEVEL
                        "exp" -> LevelType.EXP
                        else -> error("out of case")
                    }
                    try {
                        it.mark()
                        when (it.expects("to", "add", "increase")) {
                            "to" -> ProfileLevelSet(key, type, it.next(ArgTypes.ACTION), Symbol.SET)
                            "add", "increase" -> ProfileLevelSet(key, type, it.next(ArgTypes.ACTION), Symbol.ADD)
                            else -> error("out of case")
                        }
                    } catch (ex: Throwable) {
                        it.reset()
                        ProfileLevelGet(key, type)
                    }
                }
                else -> error("out of case")
            }
        }
    }
}