package ink.ptms.chemdah.module.party

import cn.mcres.iTeamPro.manager.TeamManager
import com.alessiodp.parties.api.Parties
import com.github.Shawhoi.nyteam.NyTeam
import com.pxpmc.team.TeamMain
import de.HyChrod.Party.Utilities.PartyAPI
import fw.teams.Fwteam
import ink.ptms.chemdah.api.event.PartyHookEvent
import io.izzel.taboolib.kotlin.Reflex.Companion.reflex
import io.izzel.taboolib.kotlin.Reflex.Companion.static
import io.izzel.taboolib.module.inject.TListener
import net.Indyuce.mmocore.MMOCore
import net.Indyuce.mmocore.api.player.PlayerData
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.serverct.ersha.dungeon.DungeonPlus
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
            "DungeonPlus" -> DungeonPlusHook
            "FriendsPremium" -> FriendsPremiumHook
            "iTeamPro" -> ITeamProHook
            "mcMMO" -> McMMOHook
            "MMOCore" -> MMOCoreHook
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
                    return team.members.filter { it.uniqueId != team.leader.uniqueId }.toList()
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
                    return team.reflex<Set<UUID>>("plist")!!.filter { it != team.reflex<UUID>("leader")!! }.mapNotNull { Bukkit.getPlayer(it) }
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
                    return team.members.filter { it.uniqueId != team.leader.uniqueId }
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
                    return team.teamMate.filter { it != team.teamCaptain }.mapNotNull { Bukkit.getPlayerExact(it) }
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
                    return team.teamList.filter { it.uniqueId != team.captain.uniqueId }
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
                    return team.onlineMembers.filter { team.leader != it.playerUUID }.mapNotNull { Bukkit.getPlayer(it.playerUUID) }
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

    object DungeonPlusHook : Party {

        override fun getParty(player: Player): Party.PartyInfo? {
            val team = DungeonPlus.groupManager.getGroup(player) ?: return null
            return object : Party.PartyInfo {

                override fun getLeader(): Player {
                    return team.leader
                }

                override fun getMembers(): List<Player> {
                    return team.players.filter { it.uniqueId != team.leader.uniqueId }
                }
            }
        }
    }

    object FriendsPremiumHook : Party {

        override fun getParty(player: Player): Party.PartyInfo? {
            val team = PartyAPI.getParty(player.uniqueId) ?: return null
            return object : Party.PartyInfo {

                override fun getLeader(): Player? {
                    return Bukkit.getPlayer(team.all.firstOrNull { team.isLeader(it) }?.uuid ?: return null)
                }

                override fun getMembers(): List<Player> {
                    return team.all.filter { !team.isLeader(it) }.mapNotNull { Bukkit.getPlayer(it.uuid) }
                }
            }
        }
    }

    object McMMOHook : Party {

        override fun getParty(player: Player): Party.PartyInfo? {
            val team = com.gmail.nossr50.party.PartyManager.getParty(player) ?: return null
            return object : Party.PartyInfo {

                override fun getLeader(): Player? {
                    return Bukkit.getPlayer(team.leader.uniqueId)
                }

                override fun getMembers(): List<Player> {
                    return team.onlineMembers.filter { it.uniqueId != team.leader.uniqueId }
                }
            }
        }
    }

    object MMOCoreHook : Party {

        override fun getParty(player: Player): Party.PartyInfo? {
            val teams = MMOCore.plugin.partyManager.reflex<Set<net.Indyuce.mmocore.api.player.social.Party>>("parties")!!
            val team = teams.firstOrNull { team -> team.members.reflex<List<PlayerData>>("members")!!.any { it.uniqueId == player.uniqueId } } ?: return null
            val members = team.reflex<List<PlayerData>>("members")!!
            return object : Party.PartyInfo {

                override fun getLeader(): Player? {
                    return team.owner.player
                }

                override fun getMembers(): List<Player> {
                    return members.filter { it.uniqueId != team.owner.uniqueId }.map { it.player }
                }
            }
        }
    }
}