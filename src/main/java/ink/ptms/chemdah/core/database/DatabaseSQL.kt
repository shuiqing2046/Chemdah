package ink.ptms.chemdah.core.database

import ink.ptms.chemdah.Chemdah
import ink.ptms.chemdah.core.DataContainer
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Quest
import io.izzel.taboolib.kotlin.Tasks
import io.izzel.taboolib.module.db.sql.*
import io.izzel.taboolib.module.db.sql.query.JoinWhere
import io.izzel.taboolib.module.db.sql.query.Where
import io.izzel.taboolib.module.inject.PlayerContainer
import io.izzel.taboolib.util.Coerce
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import javax.sql.DataSource
import kotlin.collections.HashMap

/**
 * Chemdah
 * ink.ptms.chemdah.database.DatabaseSQL
 *
 * @author sky
 * @since 2021/3/5 3:51 下午
 */
class DatabaseSQL : Database() {

    val host = SQLHost(Chemdah.conf.getConfigurationSection("database.source.SQL"), Chemdah.plugin, true)

    val name: String
        get() = Chemdah.conf.getString("database.source.SQL.table", "chemdah")!!

    val isServer = Chemdah.conf.getBoolean("database.source.SQL.server")

    val tableUser = SQLTable(
        "${name}_user",
        SQLColumn.PRIMARY_KEY_ID,
        SQLColumnType.VARCHAR.toColumn(36, "name").columnOptions(SQLColumnOption.UNIQUE_KEY),
        SQLColumnType.VARCHAR.toColumn(36, "uuid").columnOptions(SQLColumnOption.UNIQUE_KEY),
        SQLColumnType.DATE.toColumn("time")
    )

    val tableUserData = SQLTable(
        "${name}_user_data",
        SQLColumn.PRIMARY_KEY_ID,
        SQLColumnType.INT.toColumn(16, "user").columnOptions(SQLColumnOption.KEY),
        SQLColumnType.VARCHAR.toColumn(64, "key").columnOptions(SQLColumnOption.KEY),
        SQLColumnType.VARCHAR.toColumn(64, "value"),
        SQLColumnType.BOOL.toColumn("mode")
    )

    val tableQuest = SQLTable(
        "${name}_quest",
        SQLColumn.PRIMARY_KEY_ID,
        SQLColumnType.INT.toColumn(16, "user").columnOptions(SQLColumnOption.KEY),
        SQLColumnType.VARCHAR.toColumn(36, "quest").columnOptions(SQLColumnOption.KEY),
        SQLColumnType.BOOL.toColumn("mode")
    )

    val tableQuestData = SQLTable(
        "${name}_quest_data",
        SQLColumn.PRIMARY_KEY_ID,
        SQLColumnType.INT.toColumn(16, "quest").columnOptions(SQLColumnOption.KEY),
        SQLColumnType.VARCHAR.toColumn(64, "key").columnOptions(SQLColumnOption.KEY),
        SQLColumnType.VARCHAR.toColumn(64, "value"),
        SQLColumnType.BOOL.toColumn("mode")
    )

    val tableVariables = SQLTable(
        "${name}_variables",
        SQLColumn.PRIMARY_KEY_ID,
        SQLColumnType.VARCHAR.toColumn(64, "name").columnOptions(SQLColumnOption.UNIQUE_KEY),
        SQLColumnType.VARCHAR.toColumn(64, "data"),
        SQLColumnType.BOOL.toColumn("mode")
    )

    val dataSource: DataSource by lazy {
        host.createDataSource()
    }

    init {
        tableUser.create(dataSource)
        tableQuest.create(dataSource)
        tableUserData.create(dataSource)
        tableQuestData.create(dataSource)
        tableVariables.create(dataSource)
    }

    fun updateUserTime(userId: Long) = tableUser.update(Where.equals("id", userId)).set("time", Date()).run(dataSource)

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

    fun getQuestId(player: Player, quest: Quest): Long {
        val map = cacheQuestId.computeIfAbsent(player.name) { HashMap() }
        if (map.containsKey(quest.id)) {
            return map[quest.id]!!
        }
        val userId = tableQuest.select(Where.equals("user", getUserId(player)), Where.equals("quest", quest.id))
            .limit(1)
            .row("id")
            .to(dataSource)
            .first {
                it.getLong("id")
            } ?: return -1L
        map[quest.id] = userId
        return userId
    }

    fun PlayerProfile.init(): PlayerProfile {
        tableUserData.select(Where.equals("user", getUserId(player)), Where.equals("mode", 1))
            .row("key", "value")
            .to(dataSource)
            .map {
                it.getString("key") to it.getString("value")
            }.forEach {
                persistentDataContainer.unchanged { this[it.first] = it.second }
            }
        val quests = HashMap<String, DataContainer>()
        tableQuest.select(Where.equals("user", getUserId(player)), Where.equals("mode", 1))
            .row("quest")
            .rowOuter(tableQuestData, "key")
            .rowOuter(tableQuestData, "value")
            .innerJoin(tableQuestData, JoinWhere.equals(tableQuest, "id", tableQuestData, "quest"))
            .to(dataSource)
            .map {
                it.getString("quest") to (it.getString("key") to it.getString("value"))
            }.forEach {
                quests.computeIfAbsent(it.first) { DataContainer() }.unchanged {
                    this[it.second.first] = it.second.second
                }
            }
        quests.forEach { registerQuest(Quest(it.key, this, it.value), newQuest = false) }
        return this
    }

    fun PlayerProfile.update(player: Player) {
        val id = getUserId(player)
        persistentDataContainer.forEach { key, data ->
            if (data.changed) {
                tableUserData.update(Where.equals("user", id), Where.equals("key", key))
                    .insertIfAbsent(null, id, key, data.data, 1)
                    .set("value", data.data)
                    .set("mode", 1)
                    .run(dataSource)
            }
        }
        persistentDataContainer.drops.toList().forEach {
            tableUserData.update(Where.equals("user", id), Where.equals("key", it)).set("mode", 0).run(dataSource)
        }
        persistentDataContainer.flush()
    }

    fun PlayerProfile.updateQuest(player: Player) {
        val id = getUserId(player)
        getQuests().forEach { quest ->
            if (quest.newQuest || quest.persistentDataContainer.isChanged) {
                quest.newQuest = false
                val questId = getQuestId(player, quest)
                if (questId < 0) {
                    player.createQuest(id, quest)
                    return@forEach
                }
                quest.persistentDataContainer.forEach { key, data ->
                    if (data.changed) {
                        tableQuestData.update(Where.equals("quest", questId), Where.equals("key", key))
                            .insertIfAbsent(null, questId, key, data.data, 1)
                            .set("value", data.data)
                            .set("mode", 1)
                            .run(dataSource)
                    }
                }
                quest.persistentDataContainer.drops.toList().forEach {
                    tableQuestData.update(Where.equals("quest", questId), Where.equals("key", it)).set("mode", 0).run(dataSource)
                }
                quest.persistentDataContainer.flush()
            }
        }
    }

    fun PlayerProfile.createUser(player: Player): CompletableFuture<Long> {
        val future = CompletableFuture<Long>()
        tableUser.insert(null, player.name, player.uniqueId.toString(), Date())
            .to(dataSource)
            .statementFinish { stmt ->
                val userId = stmt.generatedKeys.run {
                    next()
                    Coerce.toLong(getObject(1))
                }
                cacheUserId[player.name] = userId
                persistentDataContainer.forEach { k, v ->
                    tableUserData.insert(null, userId, k, v.data, 1).run(dataSource)
                }
                persistentDataContainer.flush()
                getQuests().forEach {
                    player.createQuest(userId, it)
                }
                future.complete(userId)
            }.run()
        return future
    }

    fun Player.createQuest(userId: Long, quest: Quest) {
        tableQuest.insert(null, userId, quest.id, 1)
            .to(dataSource)
            .statementFinish { stmt ->
                val questId = stmt.generatedKeys.run {
                    next()
                    Coerce.toLong(getObject(1))
                }
                cacheQuestId.computeIfAbsent(name) { HashMap() }[quest.id] = questId
                quest.persistentDataContainer.forEach { k, v ->
                    tableQuestData.insert(null, questId, k, v.data, 1).run(dataSource)
                }
                quest.persistentDataContainer.flush()
            }.run()
    }

    override fun select(player: Player): PlayerProfile {
        val playerProfile = PlayerProfile(player.uniqueId)
        val user = getUserId(player)
        if (user == -1L) {
            return playerProfile
        }
        Tasks.task(true) {
            updateUserTime(user)
        }
        return playerProfile.init()
    }

    override fun update(player: Player, playerProfile: PlayerProfile) {
        val userId = getUserId(player)
        if (userId == -1L) {
            playerProfile.createUser(player)
        } else {
            playerProfile.update(player)
            playerProfile.updateQuest(player)
        }
    }

    override fun releaseQuest(player: Player, playerProfile: PlayerProfile, quest: Quest) {
        val questId = getQuestId(player, quest)
        if (questId < 0) {
            return
        }
        tableQuest.update(Where.equals("user", getUserId(player)), Where.equals("quest", quest.id))
            .set("mode", 0)
            .run(dataSource)
        tableQuestData.update(Where.equals("quest", questId))
            .set("mode", 0)
            .run(dataSource)
    }

    override fun selectVariable0(key: String): String? {
        return tableVariables.select(Where.equals("name", key), Where.equals("mode", true))
            .limit(1)
            .row("data")
            .to(dataSource)
            .first {
                it.getString("data")
            }
    }

    override fun updateVariable0(key: String, value: String) {
        tableVariables.update(Where.equals("name", key))
            .insertIfAbsent(null, key, value, true)
            .set("data", value)
            .set("mode", true)
            .run(dataSource)
    }

    override fun releaseVariable0(key: String) {
        tableVariables.update(Where.equals("name", key))
            .set("data", "")
            .set("mode", false)
            .run(dataSource)
    }

    override fun variables(): List<String> {
        return tableVariables.select(Where.equals("mode", true))
            .row("name")
            .to(dataSource)
            .map {
                it.getString("name")
            }
    }

    companion object {

        @PlayerContainer
        private val cacheUserId = ConcurrentHashMap<String, Long>()

        @PlayerContainer
        private val cacheQuestId = ConcurrentHashMap<String, MutableMap<String, Long>>()
    }
}