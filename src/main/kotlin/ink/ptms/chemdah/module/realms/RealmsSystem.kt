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
import taboolib.module.configuration.SecuredFile
import taboolib.module.nms.MinecraftVersion
import taboolib.platform.BukkitPlugin

@Awake
object RealmsSystem : Module {

    @Config("module/realms.yml")
    lateinit var conf: SecuredFile
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
    fun e(e: PlayerJoinEvent) {
        if (e.player.location.getRealms() != null) {
            Bukkit.getOnlinePlayers().forEach { player ->
                if (player.name != e.player.name) {
                    if (MinecraftVersion.majorLegacy >= 11300) {
                        player.hidePlayer(BukkitPlugin.getInstance(), e.player)
                    } else {
                        player.hidePlayer(e.player)
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun e(e: PlayerMoveEvent) {
        if (e.from.block != e.to!!.block) {
            val realms = e.to!!.getRealms()
            if (realms != null) {
                Bukkit.getOnlinePlayers().forEach { player ->
                    if (player.name != e.player.name && player.canSee(e.player)) {
                        if (MinecraftVersion.majorLegacy >= 11300) {
                            player.hidePlayer(BukkitPlugin.getInstance(), e.player)
                        } else {
                            player.hidePlayer(e.player)
                        }
                    }
                }
            } else {
                Bukkit.getOnlinePlayers().forEach { player ->
                    if (player.name != e.player.name && !player.canSee(e.player)) {
                        if (MinecraftVersion.majorLegacy >= 11300) {
                            player.showPlayer(BukkitPlugin.getInstance(), e.player)
                        } else {
                            player.showPlayer(e.player)
                        }
                    }
                }
            }
        }
    }
}