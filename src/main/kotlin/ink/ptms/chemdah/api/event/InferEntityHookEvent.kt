package ink.ptms.chemdah.api.event

import ink.ptms.chemdah.core.quest.selector.InferEntity
import taboolib.platform.type.BukkitProxyEvent

/**
 * Chemdah
 * ink.ptms.chemdah.api.event.InferEntityHookEvent
 *
 * @author sky
 * @since 2021/4/17 2:41 下午
 */
class InferEntityHookEvent(val id: String, var itemClass: Class<out InferEntity.Entity>) : BukkitProxyEvent() {

    override val allowCancelled: Boolean
        get() = false
}