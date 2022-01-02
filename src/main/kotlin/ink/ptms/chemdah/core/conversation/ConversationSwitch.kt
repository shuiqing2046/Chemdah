package ink.ptms.chemdah.core.conversation

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.api.event.collect.ConversationEvents
import ink.ptms.chemdah.util.namespace
import org.bukkit.entity.Player
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.util.asList
import taboolib.common5.Coerce
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.kether.KetherShell
import taboolib.module.kether.printKetherErrorMessage
import java.io.File
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.core.conversation.ConversationSwitch
 *
 * @author 坏黑
 * @since 2021/11/27 10:11 下午
 */
data class ConversationSwitch(val file: File?, val root: ConfigurationSection, var npcId: Trigger) {

    val cases = root.getMapList("when").map { Case((it["if"] ?: it["condition"]).toString(), it["open"].toString()) }

    fun getConversation(player: Player): CompletableFuture<Conversation?> {
        val future = CompletableFuture<Conversation?>()
        fun process(cur: Int) {
            if (cur < cases.size) {
                KetherShell.eval(cases[cur].condition, sender = adaptPlayer(player), namespace = namespace).thenAccept {
                    if (Coerce.toBoolean(it)) {
                        future.complete(cases[cur].conversation)
                    } else {
                        process(cur + 1)
                    }
                }
            }
        }
        process(0)
        return future
    }

    data class Case(val condition: String, val open: String) {

        val conversation: Conversation?
            get() = ChemdahAPI.getConversation(open)
    }

    companion object {

        val switchMap = HashMap<String, ConversationSwitch>()

        @SubscribeEvent
        internal fun e(e: ConversationEvents.Load) {
            if (e.root.contains("when")) {
                e.isCancelled = true
                val id = e.root["npc id"] ?: return
                val trigger = Trigger(id.asList().map { it.split(" ") }.filter { it.size == 2 }.map { Trigger.Id(it[0], it[1]) })
                switchMap[e.root.name] = ConversationSwitch(e.file, e.root, trigger)
            }
        }

        @SubscribeEvent
        internal fun e(e: ConversationEvents.Select) {
            if (e.conversation == null) {
                try {
                    val ele = switchMap.values.firstOrNull { it.npcId.id.any { it.isNPC(e.namespace, e.id) } } ?: return
                    ele.getConversation(e.player).thenAccept { con ->
                        e.conversation = con
                    }
                } catch (ex: Exception) {
                    ex.printKetherErrorMessage()
                }
            }
        }
    }
}