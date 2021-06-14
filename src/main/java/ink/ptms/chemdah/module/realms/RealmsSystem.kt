package ink.ptms.chemdah.module.realms

import ink.ptms.chemdah.Chemdah
import ink.ptms.chemdah.module.Module
import ink.ptms.chemdah.module.Module.Companion.register
import io.izzel.taboolib.kotlin.Tasks
import io.izzel.taboolib.kotlin.blockdb.BlockFactory
import io.izzel.taboolib.module.config.TConfig
import io.izzel.taboolib.module.inject.TInject
import io.izzel.taboolib.module.inject.TListener
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import java.util.concurrent.ConcurrentHashMap

@TListener
object RealmsSystem : Module, Listener {

    @TInject("module/realms.yml")
    lateinit var conf: TConfig
        private set

    val realmsMap = HashMap<String, Realms>()

    init {
        register()
    }

    override fun reload() {
        realmsMap.clear()
        conf.reload()
        conf.getKeys(false).forEach {
            realmsMap[it] = Realms(conf.getConfigurationSection(it)!!)
        }
    }

    fun Location.getRealms(): Realms? {
        return realmsMap.values.firstOrNull { it.world == world.name && it.area.contains(toVector()) }
    }

    @EventHandler
    fun e(e: PlayerJoinEvent) {
        if (e.player.location.getRealms() != null) {
            Bukkit.getOnlinePlayers().forEach { player ->
                if (player.name != e.player.name) {
                    player.hidePlayer(Chemdah.plugin, e.player)
                }
            }
        }
    }

    @EventHandler
    fun e(e: PlayerMoveEvent) {
        if (e.from.block != e.to.block) {
            val realms = e.to.getRealms()
            if (realms != null) {
                Bukkit.getOnlinePlayers().forEach { player ->
                    if (player.name != e.player.name) {
                        player.hidePlayer(Chemdah.plugin, e.player)
                    }
                }
            } else {
                Bukkit.getOnlinePlayers().forEach { player ->
                    if (player.name != e.player.name) {
                        player.showPlayer(Chemdah.plugin, e.player)
                    }
                }
            }
        }
    }
}