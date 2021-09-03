package ink.ptms.chemdah.api.event

import ink.ptms.chemdah.module.party.Party
import taboolib.platform.type.BukkitProxyEvent

/**
 * Chemdah
 * ink.ptms.chemdah.api.event.PartyHookEvent
 *
 * @author sky
 * @since 2021/4/24 5:36 下午
 */
class PartyHookEvent(val plugin: String, var party: Party? = null) : BukkitProxyEvent() {

    override val allowCancelled: Boolean
        get() = false
}