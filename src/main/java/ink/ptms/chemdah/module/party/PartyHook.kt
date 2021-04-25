package ink.ptms.chemdah.module.party

import cn.mcres.iTeamPro.manager.TeamManager
import com.alessiodp.parties.api.Parties
import com.github.Shawhoi.nyteam.NyTeam
import com.pxpmc.team.TeamMain
import fw.teams.Fwteam
import ink.ptms.chemdah.api.event.PartyHookEvent
import io.izzel.taboolib.kotlin.Reflex.Companion.reflex
import io.izzel.taboolib.kotlin.Reflex.Companion.static
import io.izzel.taboolib.module.inject.TListener
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import sky_bai.bukkit.baiteam.BaiTeam
import su.nightexpress.quantumrpg.api.QuantumAPI
import su.nightexpress.quantumrpg.modules.list.party.PartyManager
import java.util.*

/**
 * Chemdah
 * ink.ptms.chemdah.module.party.PartyHok
 *
 * @author sky
 * @since 2021/4/24 5:30 下午
 */
@TListener
class PartyHook : Listener {

    @EventHandler
    fun e(e: PartyHookEvent) {
        e.party = when (e.plugin) {
            "BaiTeam" -> BaiTeamHook
            "CustomGo" -> CustomGoHook
            "iTeamPro" -> ITeamProHook
            "NyTeam" -> NyTeamHook
            "PxTeam" -> PxTeamHook
            "Parties" -> PartiesHook
            "QuantumRPG", "PRORPG" -> QuantumHook
            else -> return
        }
    }

    object BaiTeamHook : Party {

        override fun getParty(player: Player): Party.PartyInfo? {
            val team = BaiTeam.getTeamManager().teams.firstOrNull { it.leaderName == player.name || player.name in it.memberNames } ?: return null
            return object : Party.PartyInfo {

                override fun getLeader(): Player? {
                    return team.leader
                }

                override fun getMembers(): List<Player> {
                    return team.members.toList()
                }
            }
        }
    }

    object CustomGoHook : Party {

        override fun getParty(player: Player): Party.PartyInfo? {
            val teams = Fwteam::class.java.static<Set<Fwteam>>("teamlist")!!
            val team = teams.firstOrNull { player.uniqueId in it.reflex<Set<UUID>>("plist")!! || player.uniqueId == it.reflex<UUID>("leader") } ?: return null
            return object : Party.PartyInfo {

                override fun getLeader(): Player? {
                    return Bukkit.getPlayer(team.reflex<UUID>("leader")!!)
                }

                override fun getMembers(): List<Player> {
                    return team.reflex<Set<UUID>>("plist")!!.mapNotNull { Bukkit.getPlayer(it) }.toList()
                }
            }
        }
    }

    object ITeamProHook : Party {

        override fun getParty(player: Player): Party.PartyInfo? {
            val team = TeamManager.getTeam(player) ?: return null
            return object : Party.PartyInfo {

                override fun getLeader(): Player? {
                    return team.leader
                }

                override fun getMembers(): List<Player> {
                    return team.members
                }
            }
        }
    }

    object NyTeamHook : Party {

        override fun getParty(player: Player): Party.PartyInfo? {
            val team = NyTeam.getNyTeamAPI().getPlayerTeam(player.name) ?: return null
            return object : Party.PartyInfo {

                override fun getLeader(): Player? {
                    return Bukkit.getPlayerExact(team.teamCaptain)
                }

                override fun getMembers(): List<Player> {
                    return team.teamMate.mapNotNull { Bukkit.getPlayerExact(it) }
                }
            }
        }
    }

    object PxTeamHook : Party {

        override fun getParty(player: Player): Party.PartyInfo? {
            val team = TeamMain.getTeamAPI().getTeam(player) ?: return null
            return object : Party.PartyInfo {

                override fun getLeader(): Player? {
                    return team.captain
                }

                override fun getMembers(): List<Player> {
                    return team.teamList
                }
            }
        }
    }

    object PartiesHook : Party {

        override fun getParty(player: Player): Party.PartyInfo? {
            val id = Parties.getApi().getPartyPlayer(player.uniqueId)?.partyId ?: return null
            val team = Parties.getApi().getParty(id) ?: return null
            return object : Party.PartyInfo {

                override fun getLeader(): Player? {
                    return Bukkit.getPlayer(team.leader ?: return null)
                }

                override fun getMembers(): List<Player> {
                    return team.onlineMembers.mapNotNull { Bukkit.getPlayer(it.playerUUID) }
                }
            }
        }
    }

    object QuantumHook : Party {

        override fun getParty(player: Player): Party.PartyInfo? {
            val teams = QuantumAPI.getModuleManager().partyManager.reflex<Map<String, PartyManager.Party>>("parties")!!
            val team = teams.values.firstOrNull { it.isMember(player) } ?: return null
            return object : Party.PartyInfo {

                override fun getLeader(): Player? {
                    return team.leader?.player
                }

                override fun getMembers(): List<Player> {
                    return team.members.filter { !it.isLeader }.mapNotNull { it.player }
                }
            }
        }
    }
}