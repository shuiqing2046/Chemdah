package ink.ptms.chemdah.core.database

import ink.ptms.chemdah.core.DataContainer
import ink.ptms.chemdah.core.DataContainer.Companion.data
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Quest
import ink.ptms.chemdah.util.asMap
import io.izzel.taboolib.module.db.local.Local
import io.izzel.taboolib.module.db.local.LocalPlayer
import org.bukkit.entity.Player

/**
 * Chemdah
 * ink.ptms.chemdah.database.DatabaseSQL
 *
 * @author sky
 * @since 2021/3/5 3:51 下午
 */
class DatabaseLocal : Database() {

    val data = Local.get().get("data/variables.yml")!!

    override fun select(player: Player): PlayerProfile {
        val playerProfile = PlayerProfile(player.uniqueId)
        val data = LocalPlayer.get(player)
        if (data.contains("Chemdah")) {
            playerProfile.persistentDataContainer.unchanged {
                merge(DataContainer(data.get("Chemdah.data").asMap().mapValues { it.value.data() }))
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
        val data = LocalPlayer.get(player)
        if (playerProfile.persistentDataContainer.changed) {
            playerProfile.persistentDataContainer.flush()
            data.set("Chemdah.data", playerProfile.persistentDataContainer.toMap())
        }
        playerProfile.quests.forEach { quest ->
            if (quest.persistentDataContainer.changed) {
                quest.persistentDataContainer.flush()
                data.set("Chemdah.quest.${quest.id}", quest.persistentDataContainer.toMap())
            }
        }
    }

    override fun releaseQuest(player: Player, playerProfile: PlayerProfile, quest: Quest) {
        LocalPlayer.get(player).set("Chemdah.quest.${quest.id}", null)
    }

    override fun selectVariable0(key: String) = data.getString(key)

    override fun updateVariable0(key: String, value: String) {
        data.set(key, value)
    }

    override fun releaseVariable0(key: String) {
        data.set(key, null)
    }

    override fun variables() = data.getKeys(false).toList()
}