package ink.ptms.chemdah.core.database

import ink.ptms.chemdah.Chemdah
import ink.ptms.chemdah.core.DataContainer
import ink.ptms.chemdah.core.DataContainer.Companion.data
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Quest
import ink.ptms.chemdah.util.asMap
import io.izzel.taboolib.cronus.bridge.CronusBridge
import io.izzel.taboolib.cronus.bridge.database.IndexType
import io.izzel.taboolib.module.locale.TLocale
import org.bukkit.Bukkit
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
class DatabaseMongoDB : Database(), Listener {

    init {
        Bukkit.getPluginManager().registerEvents(this, Chemdah.plugin)
    }

    val variablesKey = "__CHEMDAH_VARIABLES__"

    val bridge = CronusBridge.get(
        Chemdah.conf.getString("database.source.MongoDB.client"),
        Chemdah.conf.getString("database.source.MongoDB.database"),
        Chemdah.conf.getString("database.source.MongoDB.collection"),
        IndexType.UUID
    )!!

    override fun select(player: Player): PlayerProfile {
        val playerProfile = PlayerProfile(player.uniqueId)
        val data = bridge.get(player.uniqueId.toString())
        if (data.contains("Chemdah")) {
            playerProfile.persistentDataContainer.unchanged {
                data.getConfigurationSection("Chemdah.data")?.getValues(false)?.map {
                    put(it.key.replace("__point__", "."), it.value)
                }
            }
            data.getConfigurationSection("Chemdah.quest")?.getValues(false)?.forEach { (id, value) ->
                playerProfile.registerQuest(Quest(id, playerProfile).also { quest ->
                    quest.persistentDataContainer.unchanged {
                        merge(DataContainer(value.asMap().mapValues { it.value.data() }))
                    }
                })
            }
        }
        return playerProfile
    }

    override fun update(player: Player, playerProfile: PlayerProfile) {
        val data = bridge.get(player.uniqueId.toString())
        if (playerProfile.persistentDataContainer.changed) {
            playerProfile.persistentDataContainer.flush()
            data.set("Chemdah.data", playerProfile.persistentDataContainer.toMap().mapKeys { it.key.replace(".", "__point__") })
        }
        playerProfile.quests.forEach { quest ->
            if (quest.persistentDataContainer.changed) {
                quest.persistentDataContainer.flush()
                data.set("Chemdah.quest.${quest.id}", quest.persistentDataContainer.toMap())
            }
        }
        if (!data.contains("username")) {
            data.set("username", player.name)
        }
    }

    override fun releaseQuest(player: Player, playerProfile: PlayerProfile, quest: Quest) {
        bridge.get(player.uniqueId.toString()).set("Chemdah.quest.${quest.id}", null)
    }

    override fun selectVariable0(key: String) = bridge.get(variablesKey).getString(key)

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