package ink.ptms.chemdah.module.party

import cn.mcres.iTeamPro.manager.TeamManager
import com.alessiodp.parties.api.Parties
import com.github.Shawhoi.nyteam.NyTeam
import com.pxpmc.team.TeamMain
import de.HyChrod.Party.Utilities.PartyAPI
import de.erethon.dungeonsxl.DungeonsXL
import de.simonsator.partyandfriends.spigot.api.pafplayers.PAFPlayerManager
import fw.teams.Fwteam
import ink.ptms.chemdah.api.event.PartyHookEvent
import net.Indyuce.mmocore.MMOCore
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.serverct.ersha.dungeon.DungeonPlus
import org.serverct.ersha.dungeon.common.team.type.PlayerStateType
import sky_bai.bukkit.baiteam.BaiTeam
import su.nightexpress.quantumrpg.api.QuantumAPI
import su.nightexpress.quantumrpg.modules.list.party.PartyManager
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.severe
import taboolib.library.reflex.Reflex.Companion.getProperty
import java.util.*

/**
 * Chemdah
 * ink.ptms.chemdah.module.party.PartyHok
 *
 * @author sky
 * @since 2021/4/24 5:30 下午
 */
object PartyHook {

    @SubscribeEvent
    private fun onPartyHook(e: PartyHookEvent) {
        e.party = when (e.plugin) {
            "BaiTeam" -> BaiTeamHook
            "CustomGo" -> CustomGoHook
            "DungeonPlus" -> DungeonPlusHook
            "DungeonXL" -> DungeonXLHook
            "FriendsPremium" -> FriendsPremiumHook
            "iTeamPro" -> ITeamProHook
            "mcMMO" -> McMMOHook
            "MMOCore" -> MMOCoreHook
            "NyTeam" -> NyTeamHook
            "PxTeam" -> PxTeamHook
            "Parties" -> PartiesHook
            "PartyAndFriends" -> PartyAndFriendsHook
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
            val teams = Fwteam::class.java.getProperty<Set<Fwteam>>("teamlist", isStatic = true)!!
            val team = teams.firstOrNull { player.uniqueId in it.getProperty<Set<UUID>>("plist")!! || player.uniqueId == it.getProperty<UUID>("leader") }
                ?: return null
            return object : Party.PartyInfo {

                override fun getLeader(): Player? {
                    return Bukkit.getPlayer(team.getProperty<UUID>("leader")!!)
                }

                override fun getMembers(): List<Player> {
                    return team.getProperty<Set<UUID>>("plist")!!.filter { it != team.getProperty<UUID>("leader")!! }.mapNotNull { Bukkit.getPlayer(it) }
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

    object PartyAndFriendsHook : Party {

        override fun getParty(player: Player): Party.PartyInfo? {
            val p = PAFPlayerManager.getInstance().getPlayer(player.uniqueId)
            val team = de.simonsator.partyandfriends.spigot.api.party.PartyManager.getInstance().getParty(p) ?: return null
            return object : Party.PartyInfo {

                override fun getLeader(): Player? {
                    return Bukkit.getPlayer(team.leader.uniqueId)
                }

                override fun getMembers(): List<Player> {
                    return team.allPlayers.filter { it.uniqueId != team.leader.uniqueId }.mapNotNull { Bukkit.getPlayer(it.uniqueId) }
                }
            }
        }
    }

    object QuantumHook : Party {

        override fun getParty(player: Player): Party.PartyInfo? {
            val teams = QuantumAPI.getModuleManager().partyManager.getProperty<Map<String, PartyManager.Party>>("parties")!!
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
            // 1.1.3
            return try {
                val team = DungeonPlus.teamManager.getTeam(player) ?: return null
                object : Party.PartyInfo {

                    override fun getLeader(): Player {
                        return team.getLeader()
                    }

                    override fun getMembers(): List<Player> {
                        return team.getPlayers(PlayerStateType.ALL).filter { it.uniqueId != team.leader }
                    }
                }
            } catch (ex: Error) {
                error("Outdated DungeonPlus (required: >1.1.3)")
            }
        }
    }

    object DungeonXLHook : Party {

        override fun getParty(player: Player): Party.PartyInfo? {
            val team = DungeonsXL.getInstance().getPlayerGroup(player) ?: return null
            return object : Party.PartyInfo {

                override fun getLeader(): Player {
                    return team.leader
                }

                override fun getMembers(): List<Player> {
                    return team.members.onlinePlayers.filter { it.uniqueId != team.leader.uniqueId }
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
            // 版本过低 (1.10.X)
            if (kotlin.runCatching { MMOCore.plugin.dataProvider }.isFailure) {
                severe("Outdated MMOCore (required: >1.10.X)")
                return null
            }
            val data = MMOCore.plugin.dataProvider.dataManager.get(player) ?: return null
            val party = MMOCore.plugin.partyModule.getParty(data) as? net.Indyuce.mmocore.party.provided.Party ?: return null
            return object : Party.PartyInfo {

                override fun getLeader(): Player? {
                    return party.owner.player
                }

                override fun getMembers(): List<Player> {
                    return party.members.filter { it.uniqueId != party.owner.uniqueId }.map { it.player }
                }
            }
            // 老版本
//            val teams = MMOCore.plugin.partyManager.getProperty<Set<net.Indyuce.mmocore.api.player.social.Party>>("parties")!!
//            val team = teams.firstOrNull { team -> team.members.getProperty<List<PlayerData>>("members")!!.any { it.uniqueId == player.uniqueId } } ?: return null
//            val members = team.getProperty<List<PlayerData>>("members")!!
//            return object : Party.PartyInfo {
//
//                override fun getLeader(): Player? {
//                    return team.owner.player
//                }
//
//                override fun getMembers(): List<Player> {
//                    return members.filter { it.uniqueId != team.owner.uniqueId }.map { it.player }
//                }
//            }
        }
    }
}