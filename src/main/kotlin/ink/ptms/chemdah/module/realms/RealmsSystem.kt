package ink.ptms.chemdah.module.realms

import ink.ptms.chemdah.module.Module
import ink.ptms.chemdah.module.Module.Companion.register
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import taboolib.common.platform.Awake
import taboolib.common.platform.event.SubscribeEvent
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.nms.MinecraftVersion
import taboolib.platform.util.bukkitPlugin
import taboolib.platform.util.isBlockMovement

@Awake
object RealmsSystem : Module {

    @Config("module/realms.yml")
    lateinit var conf: Configuration
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
        return realmsMap.values.firstOrNull { it.world == world!!.name && it.area.contains(toVector()) }
    }

    @SubscribeEvent
    private fun onJoin(e: PlayerJoinEvent) {
        if (e.player.location.getRealms() != null) {
            Bukkit.getOnlinePlayers().forEach { player ->
                if (player.name != e.player.name) {
                    if (MinecraftVersion.majorLegacy >= 11300) {
                        player.hidePlayer(bukkitPlugin, e.player)
                    } else {
                        player.hidePlayer(e.player)
                    }
                }
            }
        }
    }

    @SubscribeEvent
    private fun onMove(e: PlayerMoveEvent) {
        if (e.isBlockMovement()) {
            val realms = e.to!!.getRealms()
            if (realms != null) {
                Bukkit.getOnlinePlayers().forEach { player ->
                    if (player.name != e.player.name && player.canSee(e.player)) {
                        if (MinecraftVersion.majorLegacy >= 11300) {
                            player.hidePlayer(bukkitPlugin, e.player)
                        } else {
                            player.hidePlayer(e.player)
                        }
                    }
                }
            } else {
                Bukkit.getOnlinePlayers().forEach { player ->
                    if (player.name != e.player.name && !player.canSee(e.player)) {
                        if (MinecraftVersion.majorLegacy >= 11300) {
                            player.showPlayer(bukkitPlugin, e.player)
                        } else {
                            player.showPlayer(e.player)
                        }
                    }
                }
            }
        }
    }
}