package ink.ptms.chemdah.module.kether

import ink.ptms.chemdah.module.level.LevelSystem
import ink.ptms.chemdah.module.level.LevelSystem.getLevel
import ink.ptms.chemdah.module.level.LevelSystem.setLevel
import ink.ptms.chemdah.util.getProfile
import ink.ptms.chemdah.util.increaseAny
import taboolib.common5.Coerce
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.ActionProfile
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class ActionProfile {

    class ProfileDataGet(val key: ParsedAction<*>, val default: ParsedAction<*> = ParsedAction.noop<Any>()) : ScriptAction<Any>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Any> {
            val future = CompletableFuture<Any>()
            frame.newFrame(key).run<Any>().thenApply {
                frame.newFrame(default).run<Any>().thenApply { def ->
                    future.complete(frame.getProfile().persistentDataContainer[it.toString()]?.data ?: def)
                }
            }
            return future
        }
    }

    class ProfileDataSet(
        val key: ParsedAction<*>,
        val value: ParsedAction<*>,
        val symbol: PlayerOperator.Method,
        val default: ParsedAction<*> = ParsedAction.noop<Any>(),
    ) : ScriptAction<Void>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Void> {
            return frame.newFrame(key).run<Any>().thenAccept { key ->
                frame.newFrame(value).run<Any>().thenAccept { value ->
                    frame.newFrame(default).run<Any>().thenAccept { def ->
                        val persistentDataContainer = frame.getProfile().persistentDataContainer
                        when {
                            value == null -> {
                                persistentDataContainer.remove(key.toString())
                            }

                            symbol == PlayerOperator.Method.INCREASE -> {
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
    }

    class ProfileDataKeys : ScriptAction<List<String>>() {

        override fun run(frame: ScriptFrame): CompletableFuture<List<String>> {
            return CompletableFuture.completedFuture(frame.getProfile().persistentDataContainer.keys())
        }
    }

    class ProfileLevelSet(val key: ParsedAction<*>, val type: LevelType, val value: ParsedAction<*>, val symbol: PlayerOperator.Method) : ScriptAction<Void>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Void> {
            return frame.newFrame(key).run<Any>().thenAccept { key ->
                frame.newFrame(value).run<Any>().thenAccept { value ->
                    val option = LevelSystem.getLevelOption(key.toString())
                    if (option != null) {
                        val playerProfile = frame.getProfile()
                        val playerLevel = option.toLevel(playerProfile.getLevel(option))
                        if (symbol == PlayerOperator.Method.INCREASE) {
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
    }

    class ProfileLevelGet(val key: ParsedAction<*>, val type: LevelType) : ScriptAction<Int>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Int> {
            val future = CompletableFuture<Int>()
            frame.newFrame(key).run<Any>().thenApply {
                val option = LevelSystem.getLevelOption(it.toString())
                if (option != null) {
                    when (type) {
                        LevelType.LEVEL -> {
                            future.complete(frame.getProfile().getLevel(option).level)
                        }
                        LevelType.EXP -> {
                            future.complete(frame.getProfile().getLevel(option).experience)
                        }
                        LevelType.EXP_MAX -> {
                            option.algorithm.getExp(frame.getProfile().getLevel(option).level).thenAccept { exp -> future.complete(exp) }
                        }
                    }
                } else {
                    future.complete(-1)
                }
            }
            return future
        }
    }

    enum class LevelType {

        LEVEL, EXP, EXP_MAX
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
        @KetherParser(["profile"], shared = true)
        fun parser() = scriptParser {
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
                            when (it.expects("+", "=", "to", "add", "increase")) {
                                "=", "to" -> ProfileDataSet(key, it.next(ArgTypes.ACTION), PlayerOperator.Method.MODIFY)
                                "+", "add", "increase" -> {
                                    val value = it.next(ArgTypes.ACTION)
                                    try {
                                        it.mark()
                                        it.expect("default")
                                        ProfileDataSet(key, value, PlayerOperator.Method.INCREASE, it.next(ArgTypes.ACTION))
                                    } catch (ex: Throwable) {
                                        it.reset()
                                        ProfileDataSet(key, value, PlayerOperator.Method.INCREASE)
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
                    val type = when (it.expects("level", "exp", "exp-max")) {
                        "level" -> LevelType.LEVEL
                        "exp" -> LevelType.EXP
                        "exp-max" -> LevelType.EXP_MAX
                        else -> error("out of case")
                    }
                    try {
                        it.mark()
                        val method = when (it.expects("+", "=", "to", "add", "increase")) {
                            "=", "to" -> PlayerOperator.Method.MODIFY
                            "+", "add", "increase" -> PlayerOperator.Method.INCREASE
                            else -> error("out of case")
                        }
                        if (type == LevelType.EXP_MAX) {
                            error("not modified")
                        }
                        ProfileLevelSet(key, type, it.next(ArgTypes.ACTION), method)
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