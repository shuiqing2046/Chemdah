package ink.ptms.chemdah.database

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.api.event.ChemdahEvents
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Quest
import ink.ptms.chemdah.util.mirrorFuture
import io.izzel.taboolib.kotlin.Tasks
import io.izzel.taboolib.module.inject.TListener
import io.izzel.taboolib.module.inject.TSchedule
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.database.Database
 *
 * @author sky
 * @since 2021/3/3 4:39 下午
 */
interface Database {

    /**
     * 从数据库拉取玩家数据
     */
    fun select(player: Player): PlayerProfile

    /**
     * 将玩家数据写入数据库
     */
    fun update(player: Player, playerProfile: PlayerProfile)

    /**
     * 释放任务数据
     */
    fun releaseQuest(player: Player, playerProfile: PlayerProfile, quest: Quest)

    @TListener
    companion object : Listener {

        val INSTANCE: Database by lazy {
            when (Type.INSTANCE) {
                Type.SQL -> DatabaseSQL()
                Type.LOCAL -> DatabaseLocal()
                Type.MONGODB -> DatabaseMongoDB()
            }
        }

        @EventHandler
        fun e(e: PlayerJoinEvent) {
            Tasks.task(true) {
                mirrorFuture("Database:select") {
                    INSTANCE.select(e.player).also {
                        ChemdahAPI.playerProfile[e.player.name] = it
                        ChemdahEvents.Selected(e.player, it).call()
                    }
                    finish()
                }
            }
        }

        @EventHandler
        fun e(e: PlayerQuitEvent) {
            val playerProfile = ChemdahAPI.playerProfile.remove(e.player.name)
            if (playerProfile?.isChanged() == true) {
                Tasks.task(true) {
                    mirrorFuture("Database:update") {
                        INSTANCE.update(e.player, playerProfile)
                        finish()
                    }
                }
            }
        }

        @TSchedule(period = 200, async = true)
        fun update200() {
            Bukkit.getOnlinePlayers().forEach {
                val playerProfile = ChemdahAPI.getPlayerProfile(it)
                if (playerProfile.isChanged()) {
                    mirrorFuture("Database:update") {
                        INSTANCE.update(it, playerProfile)
                        finish()
                    }
                }
            }
        }
    }
}