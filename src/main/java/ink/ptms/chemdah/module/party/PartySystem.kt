package ink.ptms.chemdah.module.party

import ink.ptms.chemdah.api.event.PartyHookEvent
import ink.ptms.chemdah.module.Module
import ink.ptms.chemdah.module.Module.Companion.register
import io.izzel.taboolib.module.config.TConfig
import io.izzel.taboolib.module.inject.TInject
import org.bukkit.entity.Player

object PartySystem : Module {

    @TInject("module/party.yml")
    lateinit var conf: TConfig
        private set

    val hook: Party?
        get() = PartyHookEvent(conf.getString("default.plugin", "")!!).call().party

    init {
        register()
    }

    /**
     * 通过所有支持的插件获取玩家当前所在的队伍
     */
    fun Player.getParty(): Party.PartyInfo? {
        return hook?.getParty(this)
    }
}