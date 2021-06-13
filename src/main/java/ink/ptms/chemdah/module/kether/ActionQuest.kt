package ink.ptms.chemdah.module.kether

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.core.quest.addon.AddonStats.Companion.getProgress
import ink.ptms.chemdah.core.quest.addon.AddonStats.Companion.hiddenStats
import ink.ptms.chemdah.core.quest.addon.AddonStats.Companion.refreshStats
import ink.ptms.chemdah.core.quest.addon.AddonStats.Companion.refreshStatusAlwaysType
import ink.ptms.chemdah.core.quest.addon.AddonTrack.Companion.trackQuest
import ink.ptms.chemdah.util.getProfile
import ink.ptms.chemdah.util.getQuestSelected
import ink.ptms.chemdah.util.increaseAny
import ink.ptms.chemdah.util.switch
import io.izzel.taboolib.cronus.CronusUtils
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
 * ink.ptms.chemdah.module.kether.ActionQuest
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class ActionQuest {

    class Quests(val self: Boolean) : QuestAction<List<String>>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<List<String>> {
            return CompletableFuture.completedFuture(frame.getProfile().getQuests(openAPI = !self).map { it.id })
        }

        override fun toString(): String {
            return "Quests(self=$self)"
        }
    }

    class QuestDataKeys : QuestAction<Any?>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<Any?> {
            return CompletableFuture.completedFuture(
                frame.getProfile().getQuestById(frame.getQuestSelected())?.persistentDataContainer?.keys() ?: emptyList<String>()
            )
        }

        override fun toString(): String {
            return "QuestDataKeys()"
        }
    }

    class QuestDataGet(val key: ParsedAction<*>) : QuestAction<Any?>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<Any?> {
            return frame.newFrame(key).run<Any>().thenApply {
                frame.getProfile().getQuestById(frame.getQuestSelected())?.persistentDataContainer?.get(it.toString())?.data
            }
        }

        override fun toString(): String {
            return "QuestDataGet(key=$key)"
        }
    }

    class QuestDataSet(val key: ParsedAction<*>, val value: ParsedAction<*>, val symbol: Symbol) : QuestAction<Void>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<Void> {
            return frame.newFrame(key).run<Any>().thenAccept { key ->
                frame.newFrame(value).run<Any?>().thenAccept { value ->
                    val persistentDataContainer = frame.getProfile().getQuestById(frame.getQuestSelected())?.persistentDataContainer
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

        override fun toString(): String {
            return "QuestDataSet(key=$key, value=$value, symbol=$symbol)"
        }
    }

    class QuestStats(val task: ParsedAction<*>?, val action: Action) : QuestAction<Void>() {

        enum class Action {

            HIDDEN, REFRESH
        }

        override fun process(frame: QuestContext.Frame): CompletableFuture<Void> {
            val profile = frame.getProfile()
            if (task == null) {
                profile.getQuestById(frame.getQuestSelected())?.run {
                    when (action) {
                        Action.HIDDEN -> {
                            hiddenStats(profile)
                        }
                        Action.REFRESH -> {
                            template.refreshStats(profile)
                            refreshStatusAlwaysType(profile)
                        }
                    }
                }
            } else {
                frame.newFrame(task).run<Any>().thenAccept { task ->
                    profile.getQuestById(frame.getQuestSelected())?.run {
                        if (task.toString() == "*") {
                            tasks.forEach {
                                when (action) {
                                    Action.HIDDEN -> it.hiddenStats(profile)
                                    Action.REFRESH -> it.refreshStats(profile)
                                }
                            }
                            when (action) {
                                Action.HIDDEN -> hiddenStats(profile)
                                Action.REFRESH -> template.refreshStats(profile)
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
            return CompletableFuture.completedFuture(null)
        }

        override fun toString(): String {
            return "QuestStats(task=$task, action=$action)"
        }
    }

    class QuestProgress(val task: ParsedAction<*>?, val action: Action) : QuestAction<Any>() {

        enum class Action {

            VALUE, TARGET, PERCENT, PERCENT_100
        }

        override fun process(frame: QuestContext.Frame): CompletableFuture<Any> {
            val future = CompletableFuture<Any>()
            val profile = frame.getProfile()
            val quest = profile.getQuestById(frame.getQuestSelected())
            if (quest == null) {
                future.complete("NULL")
                return future
            }
            if (task == null) {
                quest.template.getProgress(profile).thenAccept { progress ->
                    future.complete(
                        when (action) {
                            Action.VALUE -> progress.value
                            Action.TARGET -> progress.target
                            Action.PERCENT -> progress.percent
                            Action.PERCENT_100 -> Coerce.format(progress.percent * 100)
                        }
                    )
                }
            } else {
                frame.newFrame(task).run<Any>().thenAccept { task ->
                    val t = quest.getTask(task.toString())
                    if (t == null) {
                        future.complete("NULL")
                        return@thenAccept
                    }
                    t.getProgress(profile).thenAccept { progress ->
                        future.complete(
                            when (action) {
                                Action.VALUE -> progress.value
                                Action.TARGET -> progress.target
                                Action.PERCENT -> progress.percent
                                Action.PERCENT_100 -> Coerce.format(progress.percent * 100)
                            }
                        )
                    }
                }
            }
            return future
        }

        override fun toString(): String {
            return "QuestStats(task=$task, action=$action)"
        }
    }

    companion object {

        /**
         * quests
         */
        @KetherParser(["quests"])
        fun parser0() = ScriptParser.parser {
            try {
                it.mark()
                it.expect("self")
                Quests(true)
            } catch (ex: Exception) {
                it.reset()
                Quests(false)
            }
        }

        /**
         * 选择任务
         * quest select *quest
         *
         * 任务控制或判断
         * quest (accept|accepted|accept-check|complete|completed|fail|restart|stop)
         *
         * 追踪任务
         * quest track
         * quest tracking
         *
         * 任务条目
         * quest tasks
         *
         * 任务数据
         * quest data {action|*} [(add|increase|to) {action}]
         *
         * 任务进度
         * quest stats (refresh|hidden) [task {action|*}]
         *
         * 任务阶段
         * quest progress (value|target|percent|percent100) [task {action}]
         */
        @KetherParser(["quest"])
        fun parser1() = ScriptParser.parser {
            it.switch {
                case("select") {
                    val quest = it.next(ArgTypes.ACTION)
                    actionNow {
                        newFrame(quest).run<Any>().thenAccept { r ->
                            variables().set("@QuestSelected", r.toString())
                        }
                    }
                }
                case("accept") {
                    actionFuture { future ->
                        val template = ChemdahAPI.getQuestTemplate(getQuestSelected())
                        if (template == null) {
                            future.complete("NULL")
                        } else {
                            template.acceptTo(getProfile()).thenAccept { r ->
                                future.complete(r.type.toString())
                            }
                        }
                    }
                }
                case("accept-check") {
                    actionFuture { future ->
                        val template = ChemdahAPI.getQuestTemplate(getQuestSelected())
                        if (template == null) {
                            future.complete("NULL")
                        } else {
                            template.checkAccept(getProfile()).thenAccept { r ->
                                future.complete(r.type.toString())
                            }
                        }
                    }
                }
                case("accepted") {
                    actionNow {
                        getProfile().getQuestById(getQuestSelected()) != null
                    }
                }
                case("complete") {
                    actionNow {
                        getProfile().getQuestById(getQuestSelected())?.completeQuest()
                    }
                }
                case("completed") {
                    actionNow {
                        getProfile().isQuestCompleted(getQuestSelected())
                    }
                }
                case("fail", "failure") {
                    actionNow {
                        getProfile().getQuestById(getQuestSelected())?.failQuest()
                    }
                }
                case("reset", "restart") {
                    actionNow {
                        getProfile().getQuestById(getQuestSelected())?.restartQuest()
                    }
                }
                case("stop", "cancel") {
                    actionNow {
                        getProfile().unregisterQuest(getProfile().getQuestById(getQuestSelected()) ?: return@actionNow null)
                    }
                }
                case("track") {
                    actionNow {
                        val template = ChemdahAPI.getQuestTemplate(getQuestSelected())
                        if (template != null) {
                            getProfile().trackQuest = template
                        }
                    }
                }
                case("tracking") {
                    actionNow {
                        getProfile().trackQuest?.id
                    }
                }
                case("tasks") {
                    actionNow {
                        ChemdahAPI.getQuestTemplate(getQuestSelected())?.taskMap?.keys ?: emptyList<String>()
                    }
                }
                case("data") {
                    try {
                        it.mark()
                        it.expect("*")
                        QuestDataKeys()
                    } catch (ex: Throwable) {
                        it.reset()
                        val key = it.next(ArgTypes.ACTION)
                        try {
                            it.mark()
                            when (it.expects("to", "add", "increase")) {
                                "to" -> QuestDataSet(key, it.next(ArgTypes.ACTION), Symbol.SET)
                                "add", "increase" -> QuestDataSet(key, it.next(ArgTypes.ACTION), Symbol.ADD)
                                else -> error("out of case")
                            }
                        } catch (ex: Throwable) {
                            it.reset()
                            QuestDataGet(key)
                        }
                    }
                }
                case("stats", "status") {
                    val action = when (it.expects("refresh", "hide", "hidden")) {
                        "refresh" -> QuestStats.Action.REFRESH
                        "hide", "hidden" -> QuestStats.Action.HIDDEN
                        else -> error("out of case")
                    }
                    val task = try {
                        it.mark()
                        it.expect("task")
                        it.next(ArgTypes.ACTION)
                    } catch (ex: Exception) {
                        it.reset()
                        null
                    }
                    QuestStats(task, action)
                }
                case("progress") {
                    val action = when (it.expects("value", "target", "percent", "percent100")) {
                        "value" -> QuestProgress.Action.VALUE
                        "target" -> QuestProgress.Action.TARGET
                        "percent" -> QuestProgress.Action.PERCENT
                        "percent100" -> QuestProgress.Action.PERCENT_100
                        else -> error("out of case")
                    }
                    val task = try {
                        it.mark()
                        it.expect("task")
                        it.next(ArgTypes.ACTION)
                    } catch (ex: Exception) {
                        it.reset()
                        null
                    }
                    QuestProgress(task, action)
                }
            }
        }
    }
}