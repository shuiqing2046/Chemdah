package ink.ptms.chemdah.core.conversation.trigger

import ink.ptms.chemdah.api.ChemdahAPI.conversationSession
import ink.ptms.chemdah.api.event.collect.ConversationEvents
import ink.ptms.chemdah.core.conversation.ConversationManager
import ink.ptms.chemdah.core.conversation.Source
import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.npc.NPC
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.inventory.EquipmentSlot
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.util.unsafeLazy
import taboolib.common5.Coerce

/**
 * Chemdah
 * ink.ptms.chemdah.core.conversation.trigger.TriggerCitizens
 *
 * @author sky
 * @since 2021/2/9 8:46 下午
 */
internal object TriggerCitizens {

    val isCitizensHooked by unsafeLazy { Bukkit.getPluginManager().isPluginEnabled("Citizens") }

    @SubscribeEvent
    fun onBegin(e: ConversationEvents.Begin) {
        if (!isCitizensHooked) {
            return
        }
        val npc = e.session.source.entity
        if (npc is NPC && e.conversation.hasFlag("LOOK_PLAYER")) {
            npc.faceLocation(e.session.player.location)
        }
    }

    @SubscribeEvent(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onInteract(e: PlayerInteractAtEntityEvent) {
        if (!isCitizensHooked) {
            return
        }
        val player = e.player
        if (e.hand == EquipmentSlot.HAND && e.rightClicked.hasMetadata("NPC") && player.conversationSession == null) {
            val npc = CitizensAPI.getNPCRegistry().getNPC(e.rightClicked) ?: return
            val conversation = ConversationManager.getConversation(e.player, "citizens", npc.id.toString())
            if (conversation != null) {
                e.isCancelled = true
                // 打开对话
                conversation.open(e.player, object : Source<NPC>(npc.fullName, npc) {

                    override fun transfer(player: Player, newId: String): Boolean {
                        val newIdInt = Coerce.toInteger(newId)
                        val entities = e.rightClicked.getNearbyEntities(10.0, 10.0, 10.0)
                        val nearby = entities.mapNotNull { it.toNPC() }.firstOrNull { it.id == newIdInt } ?: return false
                        update(npc.fullName, nearby)
                        return true
                    }

                    override fun getOriginLocation(entity: NPC): Location {
                        return entity.entity.location.add(0.0, entity.entity.height, 0.0)
                    }
                })
            }
        }
    }

    fun Entity.toNPC(): NPC? {
        return CitizensAPI.getNPCRegistry().getNPC(this)
    }
}