package ink.ptms.chemdah.core.database

import ink.ptms.chemdah.core.DataContainer
import ink.ptms.chemdah.core.DataContainer.Companion.data
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Quest
import ink.ptms.chemdah.util.asMap
import io.izzel.taboolib.module.db.local.Local
import io.izzel.taboolib.module.db.local.LocalPlayer
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player

/**
 * Chemdah
 * ink.ptms.chemdah.database.DatabaseSQL
 *
 * @author sky
 * @since 2021/3/5 3:51 下午
 */
open class DatabaseLocal : Database() {

    val namespace = "!!chemdah!!"

    val data by lazy {
        Local.get().get("data/variables.yml")!!
    }

    open fun Player.getData(): FileConfiguration {
        return LocalPlayer.get(player)
    }

    /**
     * chemdah:
     *   data:
     *     a: b
     *     c: d
     *   quest:
     *     a:
     *       b: c
     *     d:
     *       e: f
     */
    override fun select(player: Player): PlayerProfile {
        val playerProfile = PlayerProfile(player.uniqueId)
        val data = player.getData()
        if (data.contains("Chemdah")) {
            data.getConfigurationSection("Chemdah.data")?.also {
                playerProfile.persistentDataContainer.unchanged {
                    merge(DataContainer(it.asMap().map { it.key.replace(namespace, ".") to it.value.data() }.toMap()))
                }
            }
            data.getConfigurationSection("Chemdah.quest")?.getValues(false)?.forEach { (id, value) ->
                playerProfile.registerQuest(Quest(id, playerProfile).also { quest ->
                    quest.persistentDataContainer.unchanged {
                        merge(DataContainer(value.asMap().map { it.key.replace(namespace, ".") to it.value.data() }.toMap()))
                    }
                })
            }
        }
        return playerProfile
    }

    override fun update(player: Player, playerProfile: PlayerProfile) {
        val data = player.getData()
        if (playerProfile.persistentDataContainer.isChanged) {
            playerProfile.persistentDataContainer.forEach { key, obj ->
                if (obj.changed) {
                    data.set("Chemdah.data.${key.replace(".", namespace)}", obj.value)
                }
            }
            playerProfile.persistentDataContainer.drops.forEach {
                data.set("Chemdah.data.${it.replace(".", namespace)}", null)
            }
            playerProfile.persistentDataContainer.flush()
        }
        playerProfile.getQuests().forEach { quest ->
            if (quest.persistentDataContainer.isChanged) {
                quest.persistentDataContainer.forEach { key, obj ->
                    if (obj.changed) {
                        data.set("Chemdah.quest.${quest.id}.${key.replace(".", namespace)}", obj.value)
                    }
                }
                quest.persistentDataContainer.drops.forEach {
                    data.set("Chemdah.quest.${quest.id}.${it.replace(".", namespace)}", null)
                }
                quest.persistentDataContainer.flush()
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