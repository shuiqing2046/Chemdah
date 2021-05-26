package ink.ptms.chemdah.module.party

import ink.ptms.chemdah.api.ChemdahAPI.chemdahProfile
import ink.ptms.chemdah.api.ChemdahAPI.isChemdahProfileLoaded
import ink.ptms.chemdah.api.event.PartyHookEvent
import ink.ptms.chemdah.api.event.collect.ObjectiveEvents
import ink.ptms.chemdah.api.event.collect.QuestEvents
import ink.ptms.chemdah.core.quest.AgentType
import ink.ptms.chemdah.core.quest.Quest
import ink.ptms.chemdah.core.quest.addon.AddonParty.Companion.party
import ink.ptms.chemdah.module.Module
import ink.ptms.chemdah.module.Module.Companion.register
import io.izzel.taboolib.module.config.TConfig
import io.izzel.taboolib.module.inject.TInject
import io.izzel.taboolib.module.inject.TListener
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.concurrent.ConcurrentHashMap

@TListener
object PartySystem : Module, Listener {

    @TInject("module/party.yml")
    lateinit var conf: TConfig
        private set

    private val hooks = ConcurrentHashMap<String, Party>()

    val hook: Party?
        get() {
            val id = conf.getString("default.plugin", "")!!
            if (hooks.containsKey(id)) {
                return hooks[id]
            }
            val party = PartyHookEvent(conf.getString("default.plugin", "")!!).call().party
            if (party != null) {
                hooks[id] = party
            }
            return party
        }

    init {
        register()
    }

    /**
     * 获取任务中的所有成员
     * @param self 是否包含自己
     */
    fun Quest.getMembers(self: Boolean = false): Set<Player> {
        val members = HashSet<Player>()
        val player = profile.player
        // 获取 Party 组件
        val partyAddon = template.party()
        // 是否共享任务
        if (partyAddon?.share == true && (!partyAddon.shareOnlyLeader || player.getParty()?.isLeader(player) == true)) {
            members.addAll(player.getPartyMembers())
        }
        if (self) {
            members.add(player)
        }
        return members.filter { it.isChemdahProfileLoaded }.toSet()
    }

    /**
     * 通过所有支持的插件获取玩家当前所在的队伍
     */
    fun Player.getParty(): Party.PartyInfo? {
        return hook?.getParty(this)
    }

    /**
     * 获取队伍中的所有成员
     * @param self 是否包含自己
     */
    fun Player.getPartyMembers(self: Boolean = false): Set<Player> {
        val members = HashSet<Player>()
        val team = getParty()
        if (team != null) {
            members.addAll(team.getMembers())
            val leader = team.getLeader()
            if (leader != null && leader in members) {
                members.add(leader)
            }
            if (!self) {
                members.remove(this)
            }
        }
        return members
    }

    fun shareQuests(quests: MutableList<Quest>, sharer: Player, leader: Boolean = false) {
        if (sharer.isChemdahProfileLoaded) {
            sharer.chemdahProfile.getQuests().forEach { quest ->
                val partyAddon = quest.template.party() ?: return@forEach
                // 是否分享任务
                if (partyAddon.share && (!partyAddon.shareOnlyLeader || leader)) {
                    // 是否已拥有该任务
                    if (quests.none { it.id == quest.id }) {
                        quests.add(quest)
                    }
                }
            }
        }
    }

    @EventHandler
    fun e(e: QuestEvents.Collect) {
        val team = e.playerProfile.player.getParty() ?: return
        val leader = team.getLeader()
        if (leader != null) {
            shareQuests(e.quests, leader, leader = true)
        }
        team.getMembers().forEach { member ->
            if (member.uniqueId != e.playerProfile.uniqueId) {
                shareQuests(e.quests, member)
            }
        }
    }

    @EventHandler
    fun e(e: QuestEvents.Complete.Post) {
        e.quest.profile.player.getPartyMembers().forEach { member ->
            e.quest.template.agent(member.chemdahProfile, AgentType.QUEST_COMPLETE, "party")
        }
    }

    @EventHandler
    fun e(e: QuestEvents.Failure.Post) {
        e.quest.profile.player.getPartyMembers().forEach { member ->
            e.quest.template.agent(member.chemdahProfile, AgentType.QUEST_FAILURE, "party")
        }
    }

    @EventHandler
    fun e(e: QuestEvents.Accept.Post) {
        e.quest.profile.player.getPartyMembers().forEach { member ->
            e.quest.template.agent(member.chemdahProfile, AgentType.QUEST_START, "party")
        }
    }

    @EventHandler
    fun e(e: QuestEvents.Reset.Post) {
        e.quest.profile.player.getPartyMembers().forEach { member ->
            e.quest.template.agent(member.chemdahProfile, AgentType.QUEST_RESET, "party")
        }
    }

    @EventHandler
    fun e(e: ObjectiveEvents.Complete.Post) {
        e.quest.profile.player.getPartyMembers().forEach { member ->
            e.task.agent(member.chemdahProfile, AgentType.TASK_COMPLETE, "party")
        }
    }

    @EventHandler
    fun e(e: ObjectiveEvents.Continue.Post) {
        e.quest.profile.player.getPartyMembers().forEach { member ->
            e.task.agent(member.chemdahProfile, AgentType.TASK_CONTINUE, "party")
        }
    }

    @EventHandler
    fun e(e: ObjectiveEvents.Reset.Post) {
        e.quest.profile.player.getPartyMembers().forEach { member ->
            e.task.agent(member.chemdahProfile, AgentType.TASK_RESET, "party")
        }
    }

    @EventHandler
    fun e(e: ObjectiveEvents.Continue.Pre) {
        if (!e.quest.isOwner(e.playerProfile.player) && (e.quest.template.party()?.canContinue == false || e.task.party()?.canContinue == false)) {
            e.isCancelled = true
        }
        val requireMembers = e.task.party()?.requireMembers ?: e.quest.template.party()?.requireMembers ?: 0
        if (requireMembers > 0 && requireMembers < e.playerProfile.player.getPartyMembers().size) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun e(e: ObjectiveEvents.Complete.Pre) {
        val requireMembers = e.task.party()?.requireMembers ?: e.quest.template.party()?.requireMembers ?: 0
        if (requireMembers > 0 && requireMembers < e.playerProfile.player.getPartyMembers().size) {
            e.isCancelled = true
        }
    }
}