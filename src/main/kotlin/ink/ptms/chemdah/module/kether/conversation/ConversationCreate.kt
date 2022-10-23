package ink.ptms.chemdah.module.kether.conversation

import ink.ptms.adyeshach.api.AdyeshachAPI
import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.core.conversation.trigger.TriggerAdyeshach.openConversation
import taboolib.module.chat.colored
import taboolib.module.kether.*
import taboolib.module.lang.sendLang

@KetherParser(["conversation"], namespace = "chemdah")
fun parser() = scriptParser {
    when (it.expects("self", "npc")) {
        "npc" -> {
            val id = it.nextParsedAction()
            actionFuture { f ->
                run(id).str { id ->
                    val npc = AdyeshachAPI.getVisibleEntities(player().cast()).firstOrNull { npc -> npc.id == id }
                    if (npc == null) {
                        player().sendLang("command-adyeshach-not-found")
                        f.complete(null)
                    } else {
                        f.complete(npc.openConversation(player().cast()))
                    }
                }
            }
        }
        "self" -> {
            val conversation = it.nextParsedAction()
            val name = it.nextParsedAction()
            actionFuture { f ->
                run(conversation).str { conversation ->
                    run(name).str { name ->
                        val con = ChemdahAPI.conversation[conversation]
                        if (con == null) {
                            player().sendLang("command-conversation-not-found")
                            f.complete(null)
                        } else {
                            f.complete(con.openSelf(player().cast(), name.colored()))
                        }
                    }
                }
            }
        }
        else -> error("out of case")
    }
}