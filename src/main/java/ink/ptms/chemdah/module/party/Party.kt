package ink.ptms.chemdah.module.party

import org.bukkit.entity.Player

/**
 * Chemdah
 * ink.ptms.chemdah.module.party.Party
 *
 * @author sky
 * @since 2021/4/24 12:24 上午
 */
interface Party {

    fun getParty(player: Player): PartyInfo?

    interface PartyInfo {

        fun getLeader(): Player?

        fun getMembers(): List<Player>
    }
}