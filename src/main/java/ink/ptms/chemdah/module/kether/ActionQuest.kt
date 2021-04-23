package ink.ptms.chemdah.module.kether

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.core.quest.addon.AddonStats.Companion.hiddenStats
import ink.ptms.chemdah.core.quest.addon.AddonStats.Companion.refreshStats
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
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.ActionQuest
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class ActionQuest {

    class Quests : QuestAction<List<String>>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<List<String>> {
            return CompletableFuture.completedFuture(frame.getProfile().quests.map { it.id })
        }
    }

    class QuestDataGet(val quest: ParsedAction<*>, val key: ParsedAction<*>) : QuestAction<Any?>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<Any?> {
            val future = CompletableFuture<Any?>()
            frame.newFrame(quest).run<Any>().thenApply { quest ->
                frame.newFrame(key).run<Any>().thenApply {
                    future.complete(frame.getProfile().getQuestById(quest.toString())?.persistentDataContainer?.get(it.toString())?.value)
                }
            }
            return future
        }
    }

    class QuestDataSet(val quest: ParsedAction<*>, val key: ParsedAction<*>, val value: ParsedAction<*>, val symbol: Symbol) : QuestAction<Void>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<Void> {
            return frame.newFrame(quest).run<Any>().thenAccept { quest ->
                frame.newFrame(key).run<Any>().thenAccept { key ->
                    frame.newFrame(value).run<Any?>().thenAccept { value ->
                        val persistentDataContainer = frame.getProfile().getQuestById(quest.toString())?.persistentDataContainer
                        if (persistentDataContainer != null) {
                            when {
                                value == null -> {
                                    persistentDataContainer.remove(key.toString())
                                }
                                symbol == Symbol.ADD -> {
                                    persistentDataContainer[key.toString()] = persistentDataContainer[key.toString()].increaseAny(value)
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
    }

    class QuestDataKeys(val quest: ParsedAction<*>) : QuestAction<List<String>>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<List<String>> {
            return frame.newFrame(quest).run<Any>().thenApply { quest ->
                frame.getProfile().getQuestById(quest.toString())?.persistentDataContainer?.keys() ?: emptyList()
            }
        }
    }

    class QuestAccept(val quest: ParsedAction<*>, val check: Boolean) : QuestAction<String>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<String> {
            val future = CompletableFuture<String>()
            frame.newFrame(quest).run<Any>().thenApply { quest ->
                val template = ChemdahAPI.getQuestTemplate(quest.toString())
                if (template == null) {
                    future.complete("NULL")
                } else {
                    if (check) {
                        template.checkAccept(frame.getProfile()).thenAccept {
                            future.complete(it.toString())
                        }
                    } else {
                        template.acceptTo(frame.getProfile()).thenAccept {
                            future.complete(it.toString())
                        }
                    }
                }
            }
            return future
        }
    }

    class QuestAccepted(val quest: ParsedAction<*>) : QuestAction<Boolean>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<Boolean> {
            return frame.newFrame(quest).run<Any>().thenApply { quest ->
                frame.getProfile().getQuestById(quest.toString()) != null
            }
        }
    }

    class QuestCompleted(val quest: ParsedAction<*>) : QuestAction<Boolean>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<Boolean> {
            return frame.newFrame(quest).run<Any>().thenApply { quest ->
                frame.getProfile().isQuestCompleted(quest.toString())
            }
        }
    }

    class QuestActions(val quest: ParsedAction<*>, val action: Action) : QuestAction<Void>() {

        enum class Action {

            COMPLETE, FAILURE, RESET, STOP
        }

        override fun process(frame: QuestContext.Frame): CompletableFuture<Void> {
            return frame.newFrame(quest).run<Any>().thenAccept { quest ->
                val profile = frame.getProfile()
                profile.getQuestById(quest.toString())?.run {
                    when (action) {
                        Action.COMPLETE -> completeQuest()
                        Action.FAILURE -> failureQuest()
                        Action.RESET -> resetQuest()
                        Action.STOP -> profile.unregisterQuest(this)
                    }
                }
            }
        }
    }

    class QuestStats(val quest: ParsedAction<*>, val task: ParsedAction<*>?, val action: Action) : QuestAction<Void>() {

        enum class Action {

            HIDDEN, REFRESH
        }

        override fun process(frame: QuestContext.Frame): CompletableFuture<Void> {
            return frame.newFrame(quest).run<Any>().thenAccept { quest ->
                val profile = frame.getProfile()
                if (task == null) {
                    profile.getQuestById(quest.toString())?.run {
                        when (action) {
                            Action.HIDDEN -> hiddenStats(profile)
                            Action.REFRESH -> refreshStats(profile)
                        }
                    }
                } else {
                    frame.newFrame(task).run<Any>().thenAccept { task ->
                        profile.getQuestById(quest.toString())?.run {
                            if (task.toString() == "all") {
                                tasks.forEach {
                                    when (action) {
                                        Action.HIDDEN -> it.hiddenStats(profile)
                                        Action.REFRESH -> it.refreshStats(profile)
                                    }
                                }
                            } else {
                                getTask(task.toString())?.run {
                                    when (action) {
                                        Action.HIDDEN -> hiddenStats(profile)
                                        Action.REFRESH -> refreshStats(profile)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    class QuestTasks(val quest: ParsedAction<*>) : QuestAction<List<String>>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<List<String>> {
            return frame.newFrame(quest).run<Any>().thenApply { quest ->
                ChemdahAPI.getQuestTemplate(quest.toString())?.task?.keys?.toList() ?: emptyList()
            }
        }
    }

    companion object {

        /**
         * quests
         */
        @KetherParser(["quests"])
        fun parser0() = ScriptParser.parser {
            Quests()
        }

        /**
         * quest [accept|accepted|check-accept|complete|completed|failure|reset|stop] *quest
         * quest data *quest *key
         * quest data *quest *key to *value
         * quest data *quest keys
         *
         * quest accept-check *def
         *
         * quest stats *quest hidden *1
         * quest stats *quest refresh *1
         *
         * quest tasks *quest
         */
        @KetherParser(["quest"])
        fun parser1() = ScriptParser.parser {
            when (it.expects("accept", "accept-check", "accepted", "complete", "completed", "failure", "reset", "stop", "cancel", "stats", "tasks", "data")) {
                "accept" -> QuestAccept(it.next(ArgTypes.ACTION), false)
                "accept-check" -> QuestAccept(it.next(ArgTypes.ACTION), true)
                "accepted" -> QuestAccepted(it.next(ArgTypes.ACTION))
                "complete" -> QuestActions(it.next(ArgTypes.ACTION), QuestActions.Action.COMPLETE)
                "completed" -> QuestCompleted(it.next(ArgTypes.ACTION))
                "failure" -> QuestActions(it.next(ArgTypes.ACTION), QuestActions.Action.FAILURE)
                "reset" -> QuestActions(it.next(ArgTypes.ACTION), QuestActions.Action.RESET)
                "stop", "cancel" -> QuestActions(it.next(ArgTypes.ACTION), QuestActions.Action.STOP)
                "tasks" -> QuestTasks(it.next(ArgTypes.ACTION))
                "stats" -> {
                    val action = when (it.expects("refresh", "hide", "hidden")) {
                        "refresh" -> QuestStats.Action.REFRESH
                        "hide", "hidden" -> QuestStats.Action.HIDDEN
                        else -> error("out of case")
                    }
                    val quest = it.next(ArgTypes.ACTION)
                    val task = try {
                        it.mark()
                        it.expect("task")
                        it.next(ArgTypes.ACTION)
                    } catch (ex: Exception) {
                        it.reset()
                        null
                    }
                    QuestStats(quest, task, action)
                }
                "data" -> {
                    val quest = it.next(ArgTypes.ACTION)
                    try {
                        it.mark()
                        it.expect("keys")
                        QuestDataKeys(quest)
                    } catch (ex: Throwable) {
                        it.reset()
                        val key = it.next(ArgTypes.ACTION)
                        try {
                            it.mark()
                            when (it.expects("to", "add", "increase")) {
                                "to" -> QuestDataSet(quest, key, it.next(ArgTypes.ACTION), Symbol.SET)
                                "add", "increase" -> QuestDataSet(quest, key, it.next(ArgTypes.ACTION), Symbol.ADD)
                                else -> error("out of case")
                            }
                        } catch (ex: Throwable) {
                            it.reset()
                            QuestDataGet(quest, key)
                        }
                    }
                }
                else -> error("out of case")
            }
        }
    }
}