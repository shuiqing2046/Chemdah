package ink.ptms.chemdah.util

import ink.ptms.chemdah.api.ChemdahAPI.chemdahProfile
import ink.ptms.chemdah.api.ChemdahAPI.conversationSession
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.conversation.Session
import ink.ptms.chemdah.core.quest.QuestContainer
import ink.ptms.chemdah.core.quest.Task
import ink.ptms.chemdah.core.quest.Template
import ink.ptms.chemdah.module.ui.UI
import org.bukkit.entity.Player
import taboolib.library.kether.QuestContext
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.script

val namespace = listOf(
    "adyeshach",
    "chemdah",
)

val namespaceQuest = listOf(
    "adyeshach",
    "chemdah",
    "chemdah-quest"
)

val namespaceQuestUI = listOf(
    "adyeshach",
    "chemdah",
    "chemdah-quest",
    "chemdah-quest-ui"
)

val namespaceConversationNPC = listOf(
    "adyeshach",
    "chemdah",
    "chemdah-conversation",
    "chemdah-conversation-npc"
)

val namespaceConversationPlayer = listOf(
    "adyeshach",
    "chemdah",
    "chemdah-conversation",
    "chemdah-conversation-player"
)

fun ScriptFrame.getQuestSelected(): String {
    return variables().get<Any?>("@QuestSelected").orElse(null)?.toString() ?: error("No quest selected.")
}

fun ScriptFrame.getQuestContainer(): QuestContainer {
    return variables().get<Any?>("@QuestContainer").orElse(null) as? QuestContainer ?: error("No quest container selected.")
}

fun ScriptFrame.UI(): UI {
    return variables().get<Any?>("@QuestUI").orElse(null) as? UI ?: error("No quest ui selected.")
}

fun ScriptFrame.getTemplate(): Template {
    return getQuestContainer() as Template
}

fun ScriptFrame.getTask(): Task {
    return getQuestContainer() as Task
}

fun ScriptFrame.getSession(): Session {
    return getPlayer().conversationSession ?: error("No session selected.")
}

fun ScriptFrame.getProfile(): PlayerProfile {
    return getPlayer().chemdahProfile
}

fun ScriptFrame.getPlayer(): Player {
    return script().sender?.castSafely<Player>() ?: error("No player selected.")
}

fun ScriptFrame.vars() = HashMap<String, Any?>().also { map ->
    var parent = parent()
    while (parent.isPresent) {
        map.putAll(parent.get().variables().keys().map { it to variables().get<Any>(it).orElse(null) })
        parent = parent.get().parent()
    }
    map.putAll(variables().keys().map { it to variables().get<Any>(it).orElse(null) })
}

fun ScriptFrame.rootVariables(): QuestContext.VarTable {
    var vars = variables()
    var parent = parent()
    while (parent.isPresent) {
        vars = parent.get().variables()
        parent = parent.get().parent()
    }
    return vars
}

fun Any?.increaseAny(any: Any): Any {
    this ?: return any
    return StringNumber(toString()).add(any.toString()).get()
}