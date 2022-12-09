package ink.ptms.chemdah.api.event.collect

import taboolib.platform.type.BukkitProxyEvent

/**
 * Chemdah
 * ink.ptms.chemdah.api.event.collect.PluginReloadEvent
 *
 * @author sky
 * @since 2021/4/24 5:36 下午
 */
class PluginReloadEvent {

    /** 任务重载 */
    class Quest : BukkitProxyEvent() {

        override val allowCancelled: Boolean
            get() = false
    }

    /** 模块重载 */
    class Module : BukkitProxyEvent() {

        override val allowCancelled: Boolean
            get() = false
    }

    /** 对话重载 */
    class Conversation : BukkitProxyEvent() {

        override val allowCancelled: Boolean
            get() = false
    }
}