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

    val cases = root.getMapList("when").map { Case((it["if"] ?: it["condition"]).toString(), it) }

    fun get(player: Player): CompletableFuture<Case> {
        val future = CompletableFuture<Case>()
        fun process(cur: Int) {
            if (cur < cases.size) {
                KetherShell.eval(cases[cur].condition, sender = adaptPlayer(player), namespace = namespace).thenAccept {
                    if (Coerce.toBoolean(it)) {
                        future.complete(cases[cur])
                    } else {
                        process(cur + 1)
                    }
                }
            }
        }
        process(0)
        return future
    }

    data class Case(val condition: String, val root: Map<*, *>) {

        /**
         * 兼容 Chemdah Lab
         */
        val run: List<String>?
            get() = root["run"]?.asList()?.flatMap { it.lines() }

        val open: String?
            get() = root["open"]?.toString()

        val conversation: Conversation?
            get() = if (open != null) ChemdahAPI.getConversation(open!!) else null
    }

    companion object {

        val switchMap = HashMap<String, ConversationSwitch>()

        @SubscribeEvent
        private fun onLoad(e: ConversationEvents.Load) {
            if (e.root.contains("when") && e.root.getMapList("when").isNotEmpty()) {
                e.isCancelled = true
                val id = e.root["npc id"] ?: return
                val trigger = Trigger(id.asList().map { it.split(" ") }.filter { it.size == 2 }.map { Trigger.Id(it[0], it[1]) })
                switchMap[e.root.name] = ConversationSwitch(e.file, e.root, trigger)
            }
        }

        @SubscribeEvent
        private fun onSelect(e: ConversationEvents.Select) {
            if (e.conversation == null) {
                try {
                    val ele = switchMap.values.firstOrNull { it.npcId.id.any { npc -> e.id.any { id -> npc.isNPC(e.namespace, id) } } } ?: return
                    ele.get(e.player).thenAccept { case ->
                        // 运行脚本
                        if (case.run != null) {
                            KetherShell.eval(case.run!!, sender = adaptPlayer(e.player), namespace = namespace)
                        } else {
                            // 跳转对话
                            e.conversation = case.conversation
                        }
                    }
                } catch (ex: Exception) {
                    ex.printKetherErrorMessage()
                }
            }
        }
    }
}