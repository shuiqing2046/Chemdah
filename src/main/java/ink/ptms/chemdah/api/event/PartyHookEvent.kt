package ink.ptms.chemdah.api.event

import ink.ptms.chemdah.module.party.Party
import io.izzel.taboolib.module.event.EventNormal
import org.bukkit.entity.Player

/**
 * Chemdah
 * ink.ptms.chemdah.api.event.PartyHookEvent
 *
 * @author sky
 * @since 2021/4/24 5:36 下午
 */
class PartyHookEvent(val plugin: String, var party: Party? = null) : EventNormal<PartyHookEvent>(true)