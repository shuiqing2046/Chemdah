package ink.ptms.chemdah.core.conversation.trigger

import ink.ptms.adyeshach.api.event.AdyeshachEntityInteractEvent
import ink.ptms.adyeshach.common.entity.EntityInstance
import ink.ptms.adyeshach.common.entity.ai.expand.ControllerLookAtPlayerAlways
import ink.ptms.chemdah.api.ChemdahAPI.conversationSession
import ink.ptms.chemdah.api.event.collect.ConversationEvents
import ink.ptms.chemdah.core.conversation.ConversationManager
import ink.ptms.chemdah.core.conversation.Source
import org.bukkit.Location
import org.bukkit.entity.Player
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit

/**
 * Chemdah
 * ink.ptms.chemdah.core.conversation.trigger.TriggerAdyeshach
 *
 * @author sky
 * @since 2021/2/9 8:46 下午
 */
internal object TriggerAdyeshach {

    @SubscribeEvent
    fun onBegin(e: ConversationEvents.Begin) {
        val npc = e.session.source.entity
        if (npc is EntityInstance) {
            npc.setTag("isFreeze", "true")
            npc.setTag("conversation:${e.session.player.name}", "conversation")
            // 让 NPC 看向玩家
            if (e.conversation.hasFlag("LOOK_PLAYER")) {
                // 检查 NPC 是否记录原始视角
                if (!npc.hasTag("conversation-eye-location")) {
                    npc.setTag("conversation-eye-location", "${npc.getLocation().yaw},${npc.getLocation().pitch}")
                }
                // 检查 NPC 是否持有 LookAtPlayerAlways 控制器
                if (npc.getController(ControllerLookAtPlayerAlways::class.java) == null) {
                    npc.registerController(ControllerLookAtPlayerAlways(npc))
                    npc.setTag("conversation-controller", "true")
                }
            }
        }
    }

    @SubscribeEvent
    fun onClosed(e: ConversationEvents.Closed) {
        val npc = e.session.source.entity
        if (npc is EntityInstance) {
            npc.removeTag("conversation:${e.session.player.name}")
            // 若没有玩家在与该 NPC 对话
            if (npc.getTags().none { it.value == "conversation" }) {
                // 移除冻结标记
                npc.removeTag("isFreeze")
                // 移除 LookAtPlayerAlways 控制器
                if (npc.hasTag("conversation-controller")) {
                    npc.removeTag("conversation-controller")
                    npc.unregisterController(ControllerLookAtPlayerAlways::class.java)
                }
                // 在对话结束时恢复视角
                if (e.session.conversation.hasFlag("LOOK_PLAYER") && npc.hasTag("conversation-eye-location")) {
                    val eye = npc.getTag("conversation-eye-location")!!.split(",")
                    npc.removeTag("conversation-eye-location")
                    npc.controllerLook(eye[0].toFloat(), eye[1].toFloat(), smooth = false)
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onAdyInteract(e: AdyeshachEntityInteractEvent) {
        if (e.isMainHand && e.player.conversationSession == null) {
            val conversation = ConversationManager.getConversation(e.player, "adyeshach", e.entity.id)
            if (conversation != null) {
                e.isCancelled = true
                // 同步打开对话
                submit {
                    // 创建对话源
                    val source = object : Source<EntityInstance>(e.entity.getDisplayName(), e.entity) {

                        override fun transfer(player: Player, newId: String): Boolean {
                            val entities = e.entity.manager?.getEntities()
                            val nearby = entities?.filter { it.isValidDistance(player) }?.firstOrNull { it.id == newId } ?: return false
                            update(nearby.getDisplayName(), nearby)
                            return true
                        }

                        override fun getOriginLocation(entity: EntityInstance): Location {
                            return entity.getLocation().add(0.0, entity.entityType.entitySize.height, 0.0)
                        }
                    }
                    // 打开对话
                    conversation.open(e.player, source) {
                        it.variables["@manager"] = e.entity.manager
                        it.variables["@entities"] = listOf(e.entity)
                    }
                }
            }
        }
    }

    fun EntityInstance.isValidDistance(player: Player): Boolean {
        return player.world == getWorld() && player.location.distance(getLocation()) < 10.0
    }
}