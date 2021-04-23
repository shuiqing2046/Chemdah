package ink.ptms.chemdah.api.event.single

import ink.ptms.chemdah.core.quest.selector.InferEntity
import io.izzel.taboolib.module.event.EventNormal

/**
 * Chemdah
 * ink.ptms.chemdah.api.event.single.InferEntityHookEvent
 *
 * @author sky
 * @since 2021/4/17 2:41 下午
 */
class InferEntityHookEvent(val id: String, var itemClass: Class<out InferEntity.Entity>) : EventNormal<InferEntityHookEvent>()