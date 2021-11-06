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
import taboolib.common.reflect.Reflex.Companion.invokeConstructor
import taboolib.module.configuration.Config
import taboolib.module.configuration.SecuredFile
import taboolib.module.nms.nmsClass
import taboolib.module.nms.sendPacket
import taboolib.platform.BukkitPlugin

@Awake
object Synchronous : Module {

    @Config("module/synchronous.yml")
    lateinit var conf: SecuredFile
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
    fun e(e: PlayerExpChangeEvent) {
        if (playerLevelToMinecraft != null) {
            e.amount = 0
            sendSyncLevel(e.player)
        }
    }

    @SubscribeEvent
    fun e(e: PlayerLevelChangeEvent) {
        if (playerLevelToMinecraft != null) {
            sendSyncLevel(e.player)
        }
    }

    @SubscribeEvent
    fun e(e: PlayerEvents.Selected) {
        if (playerLevelToMinecraft != null) {
            sendSyncLevel(e.player)
        }
    }

    @SubscribeEvent(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun e(e: PlayerEvents.LevelChange) {
        if (e.option.id == playerLevelToMinecraft) {
            e.option.algorithm.getExp(e.newLevel).thenAccept { exp ->
                e.player.sendPacket(packet.invokeConstructor(e.newExperience / exp.toFloat(), 0, e.newLevel))
            }
        }
    }

    fun sendSyncLevel(player: Player) {
        val option = LevelSystem.getLevelOption(playerLevelToMinecraft!!) ?: return
        val playerLevel = player.chemdahProfile.getLevel(option)
        option.algorithm.getExp(playerLevel.level).thenAccept { exp ->
            player.sendPacket(packet.invokeConstructor(playerLevel.experience / exp.toFloat(), 0, playerLevel.level))
        }
    }
}