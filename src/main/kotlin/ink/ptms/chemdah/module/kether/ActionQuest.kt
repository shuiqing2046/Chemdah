package ink.ptms.chemdah.module.kether

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.core.quest.addon.AddonStats.Companion.getProgress
import ink.ptms.chemdah.core.quest.addon.AddonStats.Companion.hiddenStats
import ink.ptms.chemdah.core.quest.addon.AddonStats.Companion.refreshStats
import ink.ptms.chemdah.core.quest.addon.AddonStats.Companion.refreshStatusAlwaysType
import ink.ptms.chemdah.core.quest.addon.AddonTrack.Companion.trackQuest
import ink.ptms.chemdah.core.quest.meta.MetaType.Companion.type
import ink.ptms.chemdah.util.getProfile
import ink.ptms.chemdah.util.getQuestSelected
import ink.ptms.chemdah.util.increaseAny
import taboolib.common5.Coerce
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.ActionQuest
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class ActionQuest {

    class Quests(val self: Boolean) : ScriptAction<List<String>>() {

        override fun run(frame: ScriptFrame): CompletableFuture<List<String>> {
            return CompletableFuture.completedFuture(frame.getProfile().getQuests(openAPI = !self).map { it.id })
        }
    }

    class QuestDataKeys : ScriptAction<Any?>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
            return CompletableFuture.completedFuture(
                frame.getProfile().getQuestById(frame.getQuestSelected())?.persistentDataContainer?.keys() ?: emptyList<String>()
            )
        }
    }

    class QuestDataGet(val key: ParsedAction<*>) : ScriptAction<Any?>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
            return frame.newFrame(key).run<Any>().thenApply {
                frame.getProfile().getQuestById(frame.getQuestSelected())?.persistentDataContainer?.get(it.toString())?.data
            }
        }
    }

    class QuestDataSet(val key: ParsedAction<*>, val value: ParsedAction<*>, val symbol: PlayerOperator.Method) : ScriptAction<Void>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Void> {
            return frame.newFrame(key).run<Any>().thenAccept { key ->
                frame.newFrame(value).run<Any?>().thenAccept { value ->
                    val persistentDataContainer = frame.getProfile().getQuestById(frame.getQuestSelected())?.persistentDataContainer
                    if (persistentDataContainer != null) {
                        when {
                            value == null -> {
                                persistentDataContainer.remove(key.toString())
                            }
                            symbol == PlayerOperator.Method.INCREASE -> {
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

    class QuestStats(val task: ParsedAction<*>?, val action: Action) : ScriptAction<Void>() {

        enum class Action {

            HIDDEN, REFRESH
        }

        override fun run(frame: ScriptFrame): CompletableFuture<Void> {
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
    }

    class QuestProgress(val task: ParsedAction<*>?, val action: Action) : ScriptAction<Any>() {

        enum class Action {

            VALUE, TARGET, PERCENT, PERCENT_100
        }

        override fun run(frame: ScriptFrame): CompletableFuture<Any> {
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
    }

    companion object {

        /**
         * quests
         */
        @KetherParser(["quests"])
        fun parser0() = scriptParser {
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
         *
         * 任务数量
         * quest count with X
         */
        @KetherParser(["quest"], shared = true)
        fun parser1() = scriptParser {
            it.switch {
                case("count") {
                    it.mark()
                    val filter = try {
                        it.expect("with")
                        it.nextToken()
                    } catch (ex: Exception) {
                        it.reset()
                        null
                    }
                    actionNow {
                        getProfile().getQuests(openAPI = true).count { filter == null || it.template.type().contains(filter) }
                    }
                }
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
                    actionTake {
                        getProfile().getQuestById(getQuestSelected())?.failQuestFuture() ?: CompletableFuture.completedFuture(null)
                    }
                }
                case("reset", "restart") {
                    actionTake {
                        getProfile().getQuestById(getQuestSelected())?.restartQuestFuture() ?: CompletableFuture.completedFuture(null)
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
                            when (it.expects("+", "=", "to", "add", "increase")) {
                                "=", "to" -> QuestDataSet(key, it.next(ArgTypes.ACTION), PlayerOperator.Method.MODIFY)
                                "+", "add", "increase" -> QuestDataSet(key, it.next(ArgTypes.ACTION), PlayerOperator.Method.INCREASE)
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