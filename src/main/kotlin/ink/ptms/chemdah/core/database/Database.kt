package ink.ptms.chemdah.core.database

import com.google.common.base.Preconditions
import ink.ptms.chemdah.Chemdah
import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.api.ChemdahAPI.chemdahProfile
import ink.ptms.chemdah.api.ChemdahAPI.isChemdahProfileLoaded
import ink.ptms.chemdah.api.event.collect.PlayerEvents
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Quest
import ink.ptms.error_reporter.ErrorAction
import ink.ptms.error_reporter.ErrorReportHandler
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Schedule
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.pluginId
import taboolib.common.platform.function.submit
import taboolib.common.platform.function.submitAsync
import taboolib.platform.util.asLangText

/**
 * Chemdah
 * ink.ptms.chemdah.core.database.Database
 *
 * @author sky
 * @since 2021/3/3 4:39 下午
 */
abstract class Database {

    /**
     * 从数据库拉取玩家数据
     */
    abstract fun select(player: Player): PlayerProfile

    /**
     * 将玩家数据写入数据库
     */
    abstract fun update(player: Player, playerProfile: PlayerProfile)

    /**
     * 释放任务数据
     */
    abstract fun releaseQuest(player: Player, playerProfile: PlayerProfile, quest: Quest)

    /**
     * 从数据库拉取全局变量
     */
    fun selectVariable(key: String): String? {
        Preconditions.checkState(key.length <= 36, "key.length > 36")
        return selectVariable0(key)
    }

    /**
     * 将全局变量写入数据库
     */
    fun updateVariable(key: String, value: String) {
        Preconditions.checkState(key.length <= 36, "key.length > 36")
        Preconditions.checkState(value.length <= 64, "value.length > 64")
        updateVariable0(key, value)
    }

    /**
     * 释放全局变量
     */
    fun releaseVariable(key: String) {
        Preconditions.checkState(key.length <= 36, "key.length > 36")
        releaseVariable0(key)
    }

    /**
     * 获取所有全局变量
     */
    abstract fun variables(): List<String>

    protected abstract fun selectVariable0(key: String): String?

    protected abstract fun updateVariable0(key: String, value: String)

    protected abstract fun releaseVariable0(key: String)

    companion object {

        val INSTANCE: Database by lazy {
            try {
                when (Type.INSTANCE) {
                    Type.SQL -> DatabaseSQL()
                    Type.LOCAL -> DatabaseSQLite()
                    Type.MONGODB -> DatabaseError(IllegalStateException())
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                DatabaseError(e)
            }
        }

        @Awake(LifeCycle.ENABLE)
        internal fun onEnable() {
            if (INSTANCE is DatabaseError && Bukkit.getPluginManager().isPluginEnabled("ErrorReporter")) {
                // 汇报异常并关服
                if (ErrorReportHandler.isRegistered()) {
                    ink.ptms.error_reporter.error(pluginId, IllegalStateException("Database Error", (INSTANCE as DatabaseError).cause), ErrorAction.SHUTDOWN)
                }
            }
        }

        @SubscribeEvent
        internal fun onLogin(e: PlayerLoginEvent) {
            if (INSTANCE is DatabaseError) {
                e.result = PlayerLoginEvent.Result.KICK_OTHER
                e.kickMessage = e.player.asLangText("database-error")
            }
        }

        @SubscribeEvent
        internal fun onJoin(e: PlayerJoinEvent) {
            submit(delay = Chemdah.conf.getLong("join-select-delay", 20), async = true) {
                if (e.player.isOnline) {
                    INSTANCE.select(e.player).also {
                        ChemdahAPI.playerProfile[e.player.name] = it
                        PlayerEvents.Selected(e.player, it).call()
                    }
                }
            }
        }

        @SubscribeEvent
        internal fun onQuit(e: PlayerQuitEvent) {
            PlayerEvents.Released(e.player).call()
        }

        @SubscribeEvent
        internal fun onKick(e: PlayerKickEvent) {
            PlayerEvents.Released(e.player).call()
        }

        @SubscribeEvent
        internal fun onReleased(e: PlayerEvents.Released) {
            val playerProfile = ChemdahAPI.playerProfile.remove(e.player.name)
            if (playerProfile?.isDataChanged == true) {
                submitAsync {
                    INSTANCE.update(e.player, playerProfile)
                    PlayerEvents.Updated(e.player, playerProfile).call()
                }
            }
        }

        @Schedule(period = 200, async = true)
        internal fun update200() {
            Bukkit.getOnlinePlayers().filter { it.isChemdahProfileLoaded }.forEach {
                val playerProfile = it.chemdahProfile
                if (playerProfile.isDataChanged) {
                    INSTANCE.update(it, playerProfile)
                    PlayerEvents.Updated(it, playerProfile).call()
                }
            }
        }

        @Awake(LifeCycle.DISABLE)
        internal fun cancel() {
            Bukkit.getOnlinePlayers().filter { it.isChemdahProfileLoaded }.forEach {
                INSTANCE.update(it, it.chemdahProfile)
            }
        }
    }
}