package ink.ptms.chemdah.core.database

import ink.ptms.chemdah.Chemdah
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Quest
import io.izzel.taboolib.cronus.bridge.CronusBridge
import io.izzel.taboolib.cronus.bridge.database.IndexType
import io.izzel.taboolib.module.locale.TLocale
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent

/**
 * Chemdah
 * ink.ptms.chemdah.database.DatabaseSQL
 *
 * @author sky
 * @since 2021/3/5 3:51 下午
 */
class DatabaseMongoDB : DatabaseLocal(), Listener {

    val variablesKey = "__CHEMDAH_VARIABLES__"

    val bridge = CronusBridge.get(
        Chemdah.conf.getString("database.source.MongoDB.client"),
        Chemdah.conf.getString("database.source.MongoDB.database"),
        Chemdah.conf.getString("database.source.MongoDB.collection"),
        IndexType.UUID
    )!!

    init {
        Bukkit.getPluginManager().registerEvents(this, Chemdah.plugin)
    }

    override fun Player.getData(): FileConfiguration {
        return bridge.get(uniqueId.toString())
    }

    override fun releaseQuest(player: Player, playerProfile: PlayerProfile, quest: Quest) {
        bridge.get(player.uniqueId.toString()).set("Chemdah.quest.${quest.id}", null)
    }

    override fun selectVariable0(key: String): String? {
        return bridge.get(variablesKey).getString(key)
    }

    override fun updateVariable0(key: String, value: String) {
        bridge.get(variablesKey).set(key, value)
    }

    override fun releaseVariable0(key: String) {
        bridge.get(variablesKey).set(key, null)
    }

    override fun variables() = bridge.get(variablesKey).getKeys(false).toList()

    @EventHandler
    fun e(e: PlayerLoginEvent) {
        if (e.player.name == variablesKey) {
            e.result = PlayerLoginEvent.Result.KICK_OTHER
            e.kickMessage = TLocale.asString("database-error-username")
        }
    }
}