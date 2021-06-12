package ink.ptms.chemdah.util

import ink.ptms.chemdah.api.ChemdahAPI.chemdahProfile
import ink.ptms.chemdah.api.ChemdahAPI.conversationSession
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.conversation.Session
import ink.ptms.chemdah.core.quest.QuestContainer
import ink.ptms.chemdah.core.quest.Task
import ink.ptms.chemdah.core.quest.Template
import ink.ptms.chemdah.module.ui.UI
import io.izzel.taboolib.cronus.util.StringNumber
import io.izzel.taboolib.kotlin.kether.Kether.expects
import io.izzel.taboolib.kotlin.kether.ScriptContext
import io.izzel.taboolib.kotlin.kether.common.api.QuestAction
import io.izzel.taboolib.kotlin.kether.common.api.QuestContext
import io.izzel.taboolib.kotlin.kether.common.loader.QuestReader
import io.izzel.taboolib.kotlin.kether.common.util.LocalizedException
import io.izzel.taboolib.kotlin.kether.script
import io.izzel.taboolib.module.locale.TLocale
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture

val namespaceQuest = listOf(
    "chemdah",
    "chemdah-quest"
)

val namespaceQuestUI = listOf(
    "chemdah",
    "chemdah-quest",
    "chemdah-quest-ui"
)

val namespaceConversationNPC = listOf(
    "chemdah",
    "chemdah-conversation",
    "chemdah-conversation-npc"
)

val namespaceConversationPlayer = listOf(
    "chemdah",
    "chemdah-conversation",
    "chemdah-conversation-player"
)

fun String.colored(): String {
    return TLocale.Translate.setColored(this)
}

fun List<String>.colored(): List<String> {
    return TLocale.Translate.setColored(this)
}

fun QuestContext.Frame.getQuestSelected(): String {
    return variables().get<Any?>("@QuestSelected").orElse(null)?.toString() ?: error("No quest selected.")
}

fun QuestContext.Frame.getQuestContainer(): QuestContainer {
    return variables().get<Any?>("@QuestContainer").orElse(null) as? QuestContainer ?: error("No quest container selected.")
}

fun QuestContext.Frame.UI(): UI {
    return variables().get<Any?>("@QuestUI").orElse(null) as? UI ?: error("No quest ui selected.")
}

fun QuestContext.Frame.getTemplate(): Template {
    return getQuestContainer() as Template
}

fun QuestContext.Frame.getTask(): Task {
    return getQuestContainer() as Task
}

fun QuestContext.Frame.getSession(): Session {
    return getPlayer().conversationSession ?: error("No session selected.")
}

fun QuestContext.Frame.getProfile(): PlayerProfile {
    return getPlayer().chemdahProfile
}

fun QuestContext.Frame.getPlayer(): Player {
    return script().sender as? Player ?: error("No player selected.")
}

fun QuestContext.Frame.vars() = HashMap<String, Any?>().also { map ->
    var parent = parent()
    while (parent.isPresent) {
        map.putAll(parent.get().variables().keys().map { it to variables().get<Any>(it).orElse(null) })
        parent = parent.get().parent()
    }
    map.putAll(variables().keys().map { it to variables().get<Any>(it).orElse(null) })
}

fun QuestContext.Frame.rootVariables(): QuestContext.VarTable {
    var vars = variables()
    var parent = parent()
    while (parent.isPresent) {
        vars = parent.get().variables()
        parent = parent.get().parent()
    }
    return vars
}

fun ScriptContext.extend(map: Map<String, Any?>) {
    rootFrame().variables().run {
        map.forEach { (k, v) -> set(k, v) }
    }
}

fun Throwable.print() {
    if (this is LocalizedException) {
        warning("Unexpected exception while parsing kether script:")
        localizedMessage.split("\n").forEach { warning(it) }
    } else {
        printStackTrace()
    }
}

fun Any?.increaseAny(any: Any): Any {
    this ?: return any
    return StringNumber(toString()).add(any.toString()).get()
}

fun QuestReader.switch(func: ExpectDSL.() -> Unit): QuestAction<*> {
    val ex = ExpectDSL()
    func(ex)
    val sel = expects(*ex.method.keys.toTypedArray())
    return ex.method[sel]!!()
}

class ExpectDSL {

    val method = HashMap<String, QuestReader.() -> QuestAction<*>>()

    fun case(vararg str: String, func: QuestReader.() -> QuestAction<*>) {
        str.forEach {
            method[it] = func
        }
    }

    fun actionNow(name: String = "chemdah-action-del", func: QuestContext.Frame.() -> Any?): QuestAction<*> {
        return object : QuestAction<Any?>() {

            override fun process(frame: QuestContext.Frame): CompletableFuture<Any?> {
                return CompletableFuture.completedFuture(func(frame))
            }

            override fun toString(): String {
                return "QuestDSL($name)"
            }
        }
    }

    fun actionFuture(name: String = "chemdah-action-del", func: QuestContext.Frame.(CompletableFuture<Any?>) -> Any?): QuestAction<*> {
        return object : QuestAction<Any?>() {

            override fun process(frame: QuestContext.Frame): CompletableFuture<Any?> {
                val future = CompletableFuture<Any?>()
                func(frame, future)
                return future
            }

            override fun toString(): String {
                return "QuestDSL($name)"
            }
        }
    }
}