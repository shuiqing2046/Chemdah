package ink.ptms.chemdah.core.database

import com.google.common.base.Preconditions
import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.api.ChemdahAPI.chemdahProfile
import ink.ptms.chemdah.api.event.PlayerEvent
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Quest
import ink.ptms.chemdah.util.colored
import ink.ptms.chemdah.util.mirrorFuture
import io.izzel.taboolib.kotlin.Tasks
import io.izzel.taboolib.module.inject.TListener
import io.izzel.taboolib.module.inject.TSchedule
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.function.Predicate

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
        updateVariable(key, value)
    }

    /**
     * 释放全局变量
     */
    fun releaseVariable(key: String) {
        Preconditions.checkState(key.length <= 36, "key.length > 36")
        releaseVariable(key)
    }

    /**
     * 获取所有全局变量
     */
    abstract fun variables(): List<String>

    protected abstract fun selectVariable0(key: String): String?

    protected abstract fun updateVariable0(key: String, value: String)

    protected abstract fun releaseVariable0(key: String)

    @TListener
    companion object : Listener {

        val INSTANCE: Database by lazy {
            try {
                when (Type.INSTANCE) {
                    Type.SQL -> DatabaseSQL()
                    Type.LOCAL -> DatabaseLocal()
                    Type.MONGODB -> DatabaseMongoDB()
                }
            } catch (e: Throwable) {
                DatabaseError(e)
            }
        }

        @EventHandler
        private fun e(e: PlayerLoginEvent) {
            if (INSTANCE is DatabaseError) {
                e.result = PlayerLoginEvent.Result.KICK_OTHER
                e.kickMessage = "&4&loERROR! &r&oThe &4&lChemdah&r&o database failed to initialize.".colored()
            }
        }

        @EventHandler
        private fun e(e: PlayerJoinEvent) {
            Tasks.task(true) {
                mirrorFuture("Database:select") {
                    INSTANCE.select(e.player).also {
                        ChemdahAPI.playerProfile[e.player.name] = it
                        PlayerEvent.Selected(e.player, it).call()
                    }
                    finish()
                }
            }
        }

        @EventHandler
        private fun e(e: PlayerQuitEvent) {
            val playerProfile = ChemdahAPI.playerProfile.remove(e.player.name)
            if (playerProfile?.changed == true) {
                Tasks.task(true) {
                    mirrorFuture("Database:update") {
                        INSTANCE.update(e.player, playerProfile)
                        PlayerEvent.Updated(e.player, playerProfile).call()
                        finish()
                    }
                }
            }
        }

        @TSchedule(period = 200, async = true)
        private fun update200() {
            Bukkit.getOnlinePlayers().forEach {
                val playerProfile = it.chemdahProfile
                if (playerProfile.changed) {
                    mirrorFuture("Database:update") {
                        INSTANCE.update(it, playerProfile)
                        PlayerEvent.Updated(it, playerProfile).call()
                        finish()
                    }
                }
            }
        }
    }
}