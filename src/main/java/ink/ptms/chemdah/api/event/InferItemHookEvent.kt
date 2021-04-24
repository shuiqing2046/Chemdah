package ink.ptms.chemdah.api.event

import ink.ptms.chemdah.core.quest.selector.InferItem
import io.izzel.taboolib.module.event.EventNormal

/**
 * Chemdah
 * ink.ptms.chemdah.api.event.InferItemHookEvent
 *
 * @author sky
 * @since 2021/4/17 2:41 下午
 */
class InferItemHookEvent(val id: String, var itemClass: Class<out InferItem.Item>) : EventNormal<InferItemHookEvent>(true)