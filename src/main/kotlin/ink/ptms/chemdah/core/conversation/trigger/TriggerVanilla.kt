package ink.ptms.chemdah.core.conversation.trigger

import ink.ptms.chemdah.api.ChemdahAPI.conversationSession
import ink.ptms.chemdah.core.conversation.ConversationManager
import ink.ptms.chemdah.core.conversation.Source
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.inventory.EquipmentSlot
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.conversation.trigger.TriggerVanilla
 *
 * @author sky
 * @since 2021/2/9 8:46 下午
 */
object TriggerVanilla {

    @SubscribeEvent(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onInteract(e: PlayerInteractAtEntityEvent) {
        if (e.hand == EquipmentSlot.HAND && e.player.conversationSession == null) {
            val name = e.rightClicked.getDisplayName()
            val conversation = ConversationManager.getConversation(e.player, "minecraft", e.rightClicked, *name)
            if (conversation != null) {
                e.isCancelled = true
                conversation.open(e.player, object : Source<Entity>(name.last(), e.rightClicked) {

                    override fun transfer(player: Player, newId: String): Boolean {
                        val entities = e.rightClicked.getNearbyEntities(10.0, 10.0, 10.0)
                        val nearby = entities.firstOrNull { it.getDisplayName().any { name -> name.equals(newId, true) } } ?: return false
                        update(nearby.getDisplayName().last(), nearby)
                        return true
                    }

                    override fun getOriginLocation(entity: Entity): Location {
                        return entity.location.add(0.0, entity.height, 0.0)
                    }
                })
            }
        }
    }

    /**
     * 获取实体展示名称
     */
    fun Entity.getDisplayName(): Array<String> {
        val names = arrayListOf(type.name)
        if (customName != null) {
            names += customName!!
        }
        return names.toTypedArray()
    }
}