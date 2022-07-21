package ink.ptms.chemdah.module.synchronous

import ink.ptms.chemdah.api.ChemdahAPI.chemdahProfile
import ink.ptms.chemdah.api.event.collect.PlayerEvents
import ink.ptms.chemdah.module.Module
import ink.ptms.chemdah.module.Module.Companion.register
import ink.ptms.chemdah.module.level.LevelSystem
import ink.ptms.chemdah.module.level.LevelSystem.getLevel
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerExpChangeEvent
import org.bukkit.event.player.PlayerLevelChangeEvent
import org.bukkit.plugin.ServicePriority
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.library.reflex.Reflex.Companion.invokeConstructor
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.SecuredFile
import taboolib.module.nms.nmsClass
import taboolib.module.nms.sendPacket
import taboolib.platform.BukkitPlugin

@Awake
object Synchronous : Module {

    @Config("module/synchronous.yml")
    lateinit var conf: Configuration
        private set

    val playerDataToVault: String?
        get() = conf.getString("synchronous.player-data-to-vault")

    val playerLevelToMinecraft: String?
        get() = conf.getString("synchronous.player-level-to-minecraft")

    val packet by lazy { nmsClass("PacketPlayOutExperience") }

    init {
        register()
    }

    @Awake(LifeCycle.LOAD)
    fun init() {
        if (Bukkit.getPluginManager().getPlugin("Vault") != null && playerDataToVault != null) {
            Bukkit.getServicesManager().register(Economy::class.java, SynchronizedVault(), BukkitPlugin.getInstance(), ServicePriority.Highest)
        }
    }

    @SubscribeEvent
    private fun onExpChange(e: PlayerExpChangeEvent) {
        if (playerLevelToMinecraft != null) {
            e.amount = 0
            sendSyncLevel(e.player)
        }
    }

    @SubscribeEvent
    private fun onLevelChange(e: PlayerLevelChangeEvent) {
        if (playerLevelToMinecraft != null) {
            sendSyncLevel(e.player)
        }
    }

    @SubscribeEvent
    private fun onPlayerEventsSelected(e: PlayerEvents.Selected) {
        if (playerLevelToMinecraft != null) {
            sendSyncLevel(e.player)
        }
    }

    @SubscribeEvent(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onPlayerEventsLevelChange(e: PlayerEvents.LevelChange) {
        if (e.option.id == playerLevelToMinecraft) {
            e.option.algorithm.getExp(e.newLevel).thenAccept { exp ->
                e.player.sendPacket(packet.invokeConstructor(e.newExperience / exp.toFloat(), 0, e.newLevel))
            }
        }
    }

    fun sendSyncLevel(player: Player) {
        conf.reload()
        val option = LevelSystem.getLevelOption(playerLevelToMinecraft!!) ?: return
        val playerLevel = player.chemdahProfile.getLevel(option)
        option.algorithm.getExp(playerLevel.level).thenAccept { exp ->
            player.sendPacket(packet.invokeConstructor(playerLevel.experience / exp.toFloat(), 0, playerLevel.level))
        }
    }
}