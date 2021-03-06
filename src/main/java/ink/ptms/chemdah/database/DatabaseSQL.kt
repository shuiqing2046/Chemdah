package ink.ptms.chemdah.database

import ink.ptms.chemdah.Chemdah
import ink.ptms.chemdah.core.DataContainer
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Quest
import io.izzel.taboolib.kotlin.Tasks
import io.izzel.taboolib.module.db.sql.*
import io.izzel.taboolib.module.db.sql.query.Where
import io.izzel.taboolib.module.inject.PlayerContainer
import org.bukkit.entity.Player
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import javax.sql.DataSource

/**
 * Chemdah
 * ink.ptms.chemdah.database.DatabaseSQL
 *
 * @author sky
 * @since 2021/3/5 3:51 下午
 */
class DatabaseSQL : Database {

    val host = SQLHost(Chemdah.conf.getConfigurationSection("database.source.SQL"), Chemdah.plugin, true)

    val name: String
        get() = Chemdah.conf.getString("database.source.SQL.table", "chemdah")!!

    val tableUser = SQLTable(
        "${name}_user",
        SQLColumn.PRIMARY_KEY_ID,
        SQLColumnType.VARCHAR.toColumn(36, "name").columnOptions(SQLColumnOption.UNIQUE_KEY),
        SQLColumnType.VARCHAR.toColumn(36, "uuid").columnOptions(SQLColumnOption.UNIQUE_KEY),
        SQLColumnType.INT.toColumn(16, "data"),
        SQLColumnType.DATE.toColumn("time")
    )

    val tableUserData = SQLTable(
        "${name}_user_data",
        SQLColumn.PRIMARY_KEY_ID,
        SQLColumnType.BLOB.toColumn("data")
    )

    val tableQuest = SQLTable(
        "${name}_quest",
        SQLColumn.PRIMARY_KEY_ID,
        SQLColumnType.VARCHAR.toColumn(36, "name").columnOptions(SQLColumnOption.KEY),
        SQLColumnType.INT.toColumn(16, "data"),
        SQLColumnType.INT.toColumn(16, "user").columnOptions(SQLColumnOption.KEY),
        SQLColumnType.BOOL.toColumn("value")
    )

    val tableQuestData = SQLTable(
        "${name}_quest_data",
        SQLColumn.PRIMARY_KEY_ID,
        SQLColumnType.BLOB.toColumn("data")
    )

    val dataSource: DataSource by lazy {
        host.createDataSource()
    }

    init {
        tableUser.create(dataSource)
        tableQuest.create(dataSource)
        tableUserData.create(dataSource)
        tableQuestData.create(dataSource)
    }

    fun getUserId(player: Player): Long {
        if (cacheUserId.containsKey(player.name)) {
            return cacheUserId[player.name]!!
        }
        val userId = tableUser.select(Where.equals("uuid", player.uniqueId.toString()))
            .limit(1)
            .row("id")
            .to(dataSource)
            .first {
                it.getLong("id")
            } ?: return -1L
        cacheUserId[player.name] = userId
        return userId
    }

    fun getUserDataId(userId: Long) = tableUser.select(Where.equals("id", userId))
        .limit(1)
        .row("data")
        .to(dataSource)
        .first {
            it.getLong("data")
        } ?: -1L

    fun getUserData(userId: Long) = tableUserData.select(Where.equals("id", getUserDataId(userId)))
        .limit(1)
        .row("data")
        .to(dataSource)
        .first {
            DataContainer.fromJson(it.getBlob("data").binaryStream.readAllBytes().toString(StandardCharsets.UTF_8))
        } ?: DataContainer()

    fun getQuest(userId: Long) = tableQuest.select(Where.equals("user", userId), Where.equals("action", true))
        .row("name", "data")
        .to(dataSource)
        .map {
            it.getLong("data") to it.getString("name")
        }.toMap()

    fun getQuestData(questId: List<Long>) = tableQuestData.select(Where.`in`("id", questId))
        .row("id", "data")
        .to(dataSource)
        .map {
            it.getLong("id") to DataContainer.fromJson(it.getBlob("data").binaryStream.readAllBytes().toString(StandardCharsets.UTF_8))
        }.toMap()

    fun getQuestDataId(userId: Long, quest: Quest) = tableQuest.select(Where.equals("user", userId), Where.equals("name", quest.id))
        .limit(1)
        .row("data")
        .to(dataSource)
        .first {
            it.getLong("data")
        } ?: -1L

    fun updateUserTime(userId: Long) = tableUser.update(Where.equals("id", userId))
        .set("time", Date())
        .run(dataSource)

    fun createUser(player: Player, playerProfile: PlayerProfile): CompletableFuture<Long> {
        val userId = CompletableFuture<Long>()
        CompletableFuture<Void>().also { future ->
            tableUserData.insert(null, playerProfile.persistentDataContainer.toJson().toByteArray(StandardCharsets.UTF_8))
                .to(dataSource)
                .statement { stmt ->
                    future.thenApply {
                        CompletableFuture<Void>().also { future ->
                            tableUser.insert(null, player.name, player.uniqueId, stmt.generatedKeys.getLong("id"), Date())
                                .to(dataSource)
                                .statement { stmt ->
                                    future.thenApply {
                                        userId.complete(stmt.generatedKeys.getLong("id"))
                                    }
                                }.run()
                            future.complete(null)
                        }
                    }
                }.run()
            future.complete(null)
        }
        return userId
    }

    fun createQuest(userId: Long, quest: Quest) {
        CompletableFuture<Void>().also { future ->
            tableQuestData.insert(null, quest.persistentDataContainer.toJson().toByteArray(StandardCharsets.UTF_8))
                .to(dataSource)
                .statement { stmt ->
                    future.thenApply {
                        tableQuest.insert(null, quest.id, stmt.generatedKeys.getLong("id"), userId, true).run(dataSource)
                    }
                }
            future.complete(null)
        }
    }

    override fun select(player: Player): PlayerProfile {
        val playerProfile = PlayerProfile(player.uniqueId)
        val user = getUserId(player)
        if (user == -1L) {
            return playerProfile
        }
        playerProfile.persistentDataContainer.unchanged {
            merge(getUserData(user))
        }
        val quest = getQuest(user)
        getQuestData(quest.keys.toList()).forEach { data ->
            playerProfile.registerQuest(Quest(quest[data.key]!!, playerProfile).also { q ->
                q.persistentDataContainer.unchanged {
                    merge(data.value)
                }
            })
        }
        Tasks.task(true) {
            updateUserTime(user)
        }
        return playerProfile
    }

    override fun update(player: Player, playerProfile: PlayerProfile) {
        val user = getUserId(player)
        if (user == -1L) {
            createUser(player, playerProfile).thenApply { userId ->
                playerProfile.quests.forEach {
                    createQuest(userId, it)
                }
            }
        } else {
            if (playerProfile.persistentDataContainer.changed) {
                playerProfile.persistentDataContainer.flush()
                tableUserData.update(Where.equals("id", getUserDataId(user)))
                    .set("data", playerProfile.persistentDataContainer.toJson().toByteArray(StandardCharsets.UTF_8))
                    .run(dataSource)
            }
            playerProfile.quests.forEach { quest ->
                if (quest.persistentDataContainer.changed) {
                    quest.persistentDataContainer.flush()
                    val questDataId = getQuestDataId(user, quest)
                    if (questDataId == -1L) {
                        createQuest(user, quest)
                    } else {
                        tableQuest.update(Where.equals("user", user), Where.equals("name", quest.id))
                            .set("action", true)
                            .run(dataSource)
                        tableQuestData.update(Where.equals("id", questDataId))
                            .set("data", quest.persistentDataContainer.toJson().toByteArray(StandardCharsets.UTF_8))
                            .run(dataSource)
                    }
                }
            }
        }
    }

    override fun releaseQuest(player: Player, playerProfile: PlayerProfile, quest: Quest) {
        val user = getUserId(player)
        if (user == -1L) {
            return
        }
        val questDataId = getQuestDataId(user, quest)
        if (questDataId == -1L) {
            return
        }
        tableQuest.update(Where.equals("user", user), Where.equals("name", quest.id))
            .set("action", false)
            .run(dataSource)
        tableQuestData.update(Where.equals("id", questDataId))
            .set("data", ByteArray(0))
            .run(dataSource)
    }

    companion object {

        @PlayerContainer
        private val cacheUserId = ConcurrentHashMap<String, Long>()
    }
}