package ink.ptms.chemdah.module.kether.conversation

import ink.ptms.adyeshach.api.AdyeshachAPI
import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.core.conversation.trigger.TriggerAdyeshach.openConversation
import taboolib.common.platform.function.submit
import taboolib.module.chat.colored
import taboolib.module.kether.*
import taboolib.module.lang.sendLang

object ConversationCreate {

    /**
     * conversation npc id force-looking
     */
    @KetherParser(["conversation"], namespace = "chemdah")
    fun parser() = scriptParser {
        when (it.expects("npc", "self")) {
            "npc" -> {
                val id = it.nextParsedAction()
                val forceLooking = try {
                    it.mark()
                    it.expects("force-looking")
                    true
                } catch (ex: Exception) {
                    it.reset()
                    false
                }
                actionNow {
                    run(id).str { id ->
                        val npc = AdyeshachAPI.getVisibleEntities(player().cast()).firstOrNull { npc -> npc.id == id }
                        if (npc == null) {
                            player().sendLang("command-adyeshach-not-found")
                        } else {
                            submit { npc.openConversation(player().cast(), look = forceLooking) }
                        }
                    }
                }
            }
            "self" -> {
                val conversation = it.nextParsedAction()
                val name = it.nextParsedAction()
                actionNow {
                    run(conversation).str { conversation ->
                        run(name).str { name ->
                            val con = ChemdahAPI.conversation[conversation]
                            submit { con?.openSelf(player().cast(), name.colored()) ?: player().sendLang("command-conversation-not-found") }
                        }
                    }
                }
            }
            else -> error("out of case")
        }
    }

}