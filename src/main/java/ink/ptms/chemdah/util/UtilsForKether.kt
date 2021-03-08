package ink.ptms.chemdah.util

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.conversation.Session
import ink.ptms.chemdah.core.quest.Quest
import ink.ptms.chemdah.core.quest.QuestContainer
import ink.ptms.chemdah.core.quest.Task
import ink.ptms.chemdah.core.quest.Template
import io.izzel.taboolib.kotlin.kether.ScriptContext
import io.izzel.taboolib.kotlin.kether.common.api.QuestContext
import io.izzel.taboolib.kotlin.kether.common.util.LocalizedException
import io.izzel.taboolib.module.locale.chatcolor.TColor
import org.bukkit.entity.Player

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
    return TColor.translate(this)
}

fun QuestContext.Frame.getQuest(): Quest {
    return variables().get<Any?>("@Quest").orElse(null) as? Quest ?: error("No quest selected.")
}

fun QuestContext.Frame.getQuestContainer(): QuestContainer {
    return variables().get<Any?>("@QuestContainer").orElse(null) as? QuestContainer ?: error("No quest container selected.")
}

fun QuestContext.Frame.getTemplate(): Template {
    return getQuestContainer() as Template
}

fun QuestContext.Frame.getTask(): Task {
    return getQuestContainer() as Task
}

fun QuestContext.Frame.getSession(): Session {
    val player = (context() as ScriptContext).sender as? Player ?: error("No player selected.")
    return ChemdahAPI.getConversationSession(player) ?: error("No session selected.")
}

fun QuestContext.Frame.getProfile(): PlayerProfile {
    val player = (context() as ScriptContext).sender as? Player ?: error("No player selected.")
    return ChemdahAPI.getPlayerProfile(player)
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