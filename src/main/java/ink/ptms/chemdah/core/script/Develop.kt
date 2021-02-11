package ink.ptms.chemdah.core.script

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.core.conversation.AgentType
import ink.ptms.chemdah.core.conversation.Session
import io.izzel.taboolib.kotlin.kether.ScriptContext
import io.izzel.taboolib.kotlin.kether.common.api.QuestContext
import io.izzel.taboolib.kotlin.kether.common.util.LocalizedException
import io.izzel.taboolib.module.locale.chatcolor.TColor
import org.bukkit.entity.Player

fun QuestContext.Frame.getSession(): Session {
    val player = (context() as ScriptContext).sender as? Player ?: error("No player selected.")
    return ChemdahAPI.getConversationSession(player) ?: error("No session selected.")
}

fun QuestContext.Frame.vars() = HashMap<String, Any?>().also { map ->
    var parent = parent()
    while (parent.isPresent) {
        map.putAll(parent.get().variables().keys().map { it to variables().get<Any>(it).orElse(null) })
        parent = parent.get().parent()
    }
    map.putAll(variables().keys().map { it to variables().get<Any>(it).orElse(null) })
}

fun ScriptContext.extend(map: Map<String, Any?>) {
    rootFrame().variables().run {
        map.forEach { (k, v) -> set(k, v) }
    }
}

fun LocalizedException.print() {
    println("[Chemdah] Unexpected exception while parsing kether shell:")
    localizedMessage.split("\n").forEach {
        println("[Chemdah] $it")
    }
}

fun String.colored(): String {
    return TColor.translate(this)
}

fun AgentType.namespace() = listOf(
    "chemdah",
    "chemdah:conversation",
    "chemdah:conversation:${namespace}"
)

val namespaceConversationNPC = listOf(
    "chemdah",
    "chemdah:conversation",
    "chemdah:conversation:npc"
)

val namespaceConversationPlayer = listOf(
    "chemdah",
    "chemdah:conversation",
    "chemdah:conversation:player"
)