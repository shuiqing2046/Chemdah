package ink.ptms.chemdah.core.database

import ink.ptms.chemdah.Chemdah
import ink.ptms.chemdah.core.DataContainer
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Quest
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.common5.Coerce
import taboolib.module.database.ColumnOptionSQL
import taboolib.module.database.ColumnTypeSQL
import taboolib.module.database.Table
import taboolib.module.database.getHost
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
class DatabaseSQL : Database() {

    val host = Chemdah.conf.getHost("database.source.SQL")

    val name: String
        get() = Chemdah.conf.getString("database.source.SQL.table", "chemdah")!!

    val tableUser = Table("${name}_user", host) {
        add { id() }
        add("name") {
            type(ColumnTypeSQL.VARCHAR, 36) {
                options(ColumnOptionSQL.UNIQUE_KEY)
            }
        }
        add("uuid") {
            type(ColumnTypeSQL.VARCHAR, 36) {
                options(ColumnOptionSQL.UNIQUE_KEY)
            }
        }
        add("time") {
            type(ColumnTypeSQL.DATE)
        }
    }

    val tableUserData = Table("${name}_user_data", host) {
        add { id() }
        add("user") {
            type(ColumnTypeSQL.INT, 16) {
                options(ColumnOptionSQL.KEY)
            }
        }
        add("key") {
            type(ColumnTypeSQL.VARCHAR, 64) {
                options(ColumnOptionSQL.KEY)
            }
        }
        add("value") {
            type(ColumnTypeSQL.VARCHAR, 64)
        }
        add("mode") {
            type(ColumnTypeSQL.BOOL)
        }
    }

    val tableQuest = Table("${name}_quest", host) {
        add { id() }
        add("user") {
            type(ColumnTypeSQL.INT, 16) {
                options(ColumnOptionSQL.KEY)
            }
        }
        add("quest") {
            type(ColumnTypeSQL.VARCHAR, 36) {
                options(ColumnOptionSQL.KEY)
            }
        }
        add("mode") {
            type(ColumnTypeSQL.BOOL)
        }
    }

    val tableQuestData = Table("${name}_quest_data", host) {
        add { id() }
        add("quest") {
            type(ColumnTypeSQL.INT, 16) {
                options(ColumnOptionSQL.KEY)
            }
        }
        add("key") {
            type(ColumnTypeSQL.VARCHAR, 64) {
                options(ColumnOptionSQL.KEY)
            }
        }
        add("value") {
            type(ColumnTypeSQL.VARCHAR, 64)
        }
        add("mode") {
            type(ColumnTypeSQL.BOOL)
        }
    }

    val tableVariables = Table("${name}_variables", host) {
        add { id() }
        add("name") {
            type(ColumnTypeSQL.VARCHAR, 64) {
                options(ColumnOptionSQL.UNIQUE_KEY)
            }
        }
        add("data") {
            type(ColumnTypeSQL.VARCHAR, 64)
        }
        add("mode") {
            type(ColumnTypeSQL.BOOL)
        }
    }

    val dataSource: DataSource by lazy {
        host.createDataSource()
    }

    init {
        tableUser.workspace(dataSource) { createTable() }.run()
        tableQuest.workspace(dataSource) { createTable() }.run()
        tableUserData.workspace(dataSource) { createTable() }.run()
        tableQuestData.workspace(dataSource) { createTable() }.run()
        tableVariables.workspace(dataSource) { createTable() }.run()
    }

    fun updateUserTime(userId: Long) {
        tableUser.workspace(dataSource) {
            update {
                where {
                    "id" eq userId
                }
                set("time", Date())
            }
        }.run()
    }

    fun getUserId(player: Player): Long {
        if (cacheUserId.containsKey(player.name)) {
            return cacheUserId[player.name]!!
        }
        val userId = tableUser.workspace(dataSource) {
            select {
                where {
                    "uuid" eq player.uniqueId.toString()
                }
                limit(1)
                rows("id")
            }
        }.firstOrNull {
            getLong("id")
        } ?: -1L
        cacheUserId[player.name] = userId
        return userId
    }

    fun getQuestId(player: Player, quest: Quest): Long {
        val map = cacheQuestId.computeIfAbsent(player.name) { HashMap() }
        if (map.containsKey(quest.id)) {
            return map[quest.id]!!
        }
        val userId = tableQuest.workspace(dataSource) {
            select {
                where {
                    and {
                        "uuid" eq getUserId(player)
                        "quest" eq quest.id
                    }
                }
                limit(1)
                rows("id")
            }
        }.firstOrNull {
            getLong("id")
        } ?: -1L
        map[quest.id] = userId
        return userId
    }

    fun PlayerProfile.init(): PlayerProfile {
        tableUserData.workspace(dataSource) {
            select {
                where {
                    and {
                        "user" eq getUserId(player)
                        "mode" eq 1
                    }
                }
                rows("key", "value")
            }
        }.map {
            getString("key") to getString("value")
        }.forEach {
            persistentDataContainer.unchanged { this[it.first] = it.second }
        }
        val quests = HashMap<String, DataContainer>()
        tableQuest.workspace(dataSource) {
            select {
                rows("quest", "${tableQuestData.name}.key", "${tableQuestData.name}.value")
                where {
                    and {
                        "user" eq getUserId(player)
                        "mode" eq 1
                    }
                }
                innerJoin(tableQuestData.name) {
                    where { "${tableQuest.name}.id" eq pre("${tableQuestData.name}.quest") }
                }
            }
        }.map {
            getString("quest") to (getString("key") to getString("value"))
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
        persistentDataContainer.forEach { (key, data) ->
            if (data.changed) {
                if (tableUserData.workspace(dataSource) { select { where { "user" eq id; "key" eq key } } }.find()) {
                    tableUserData.workspace(dataSource) {
                        update {
                            where {
                                "user" eq id
                                "key" eq key
                            }
                            set("value", data.data)
                            set("mode", 1)
                        }
                    }.run()
                } else {
                    tableUserData.workspace(dataSource) {
                        insert("user", "key", "value", "mode") { value(id, key, data.data, 1) }
                    }.run()
                }
            }
        }
        tableUserData.workspace(dataSource) {
            update {
                where {
                    "user" eq id
                    "key" inside persistentDataContainer.drops.toTypedArray()
                }
                set("mode", 0)
            }
        }.run()
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
                quest.persistentDataContainer.forEach { (key, data) ->
                    if (data.changed) {
                        if (tableQuestData.workspace(dataSource) { select { where { "quest" eq questId; "key" eq key } } }.find()) {
                            tableQuestData.workspace(dataSource) {
                                update {
                                    where {
                                        "quest" eq questId
                                        "key" eq key
                                    }
                                    set("value", data.data)
                                    set("mode", 1)
                                }
                            }.run()
                        } else {
                            tableQuestData.workspace(dataSource) {
                                insert("quest", "key", "value", "mode") { value(questId, key, data.data, 1) }
                            }.run()
                        }
                    }
                }
                quest.persistentDataContainer.drops.toList().forEach {
                    tableQuestData.workspace(dataSource) {
                        update {
                            where {
                                "quest" eq questId
                                "key" eq it
                            }
                            set("mode", 0)
                        }
                    }.run()
                }
                quest.persistentDataContainer.flush()
            }
        }
    }

    fun PlayerProfile.createUser(player: Player): CompletableFuture<Long> {
        val future = CompletableFuture<Long>()
        tableUser.workspace(dataSource) {
            insert("name", "uuid", "time") {
                value(player.name, player.uniqueId.toString(), Date())
                onFinally {
                    val userId = generatedKeys.run {
                        next()
                        Coerce.toLong(getObject(1))
                    }
                    cacheUserId[player.name] = userId
                    tableUserData.workspace(dataSource) {
                        insert("user", "key", "value", "mode") {
                            persistentDataContainer.forEach { (k, v) ->
                                value(userId, k, v.data, 1)
                            }
                        }
                    }.run()
                    persistentDataContainer.flush()
                    getQuests().forEach { player.createQuest(userId, it) }
                    future.complete(userId)
                }
            }
        }.run()
        return future
    }

    fun Player.createQuest(userId: Long, quest: Quest) {
        tableQuest.workspace(dataSource) {
            insert("user", "quest", "mode") {
                value(userId, quest.id, 1)
                onFinally {
                    val questId = generatedKeys.run {
                        next()
                        Coerce.toLong(getObject(1))
                    }
                    cacheQuestId.computeIfAbsent(name) { HashMap() }[quest.id] = questId
                    tableQuestData.workspace(dataSource) {
                        insert("quest", "key", "value", "mode") {
                            quest.persistentDataContainer.forEach { (k, v) ->
                                value(questId, k, v.data, 1)
                            }
                        }
                    }.run()
                    quest.persistentDataContainer.flush()
                }
            }
        }.run()
    }

    override fun select(player: Player): PlayerProfile {
        val playerProfile = PlayerProfile(player.uniqueId)
        val user = getUserId(player)
        if (user == -1L) {
            return playerProfile
        }
        submit(async = true) { updateUserTime(user) }
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
        tableQuest.workspace(dataSource) {
            update {
                where {
                    and {
                        "user" eq getUserId(player)
                        "quest" eq quest.id
                    }
                }
                set("mode", 0)
            }
        }.run()
        tableQuestData.workspace(dataSource) {
            update {
                where {
                    "quest" eq questId
                }
                set("mode", 0)
            }
        }.run()
    }

    override fun selectVariable0(key: String): String? {
        return tableVariables.workspace(dataSource) {
            select {
                where {
                    and {
                        "name" eq key
                        "mode" eq true
                    }
                }
                limit(1)
                rows("data")
            }
        }.firstOrNull {
            getString("data")
        }
    }

    override fun updateVariable0(key: String, value: String) {
        if (tableVariables.workspace(dataSource) { select { where { "name" eq key } } }.find()) {
            tableVariables.workspace(dataSource) {
                update {
                    where {
                        "name" eq key
                    }
                    set("data", value)
                    set("mode", true)
                }
            }.run()
        } else {
            tableVariables.workspace(dataSource) {
                insert("name", "data", "mode") { value(key, value, true) }
            }.run()
        }
    }

    override fun releaseVariable0(key: String) {
        tableVariables.workspace(dataSource) {
            update {
                where {
                    "name" eq key
                }
                set("data", "")
                set("mode", false)
            }
        }.run()
    }

    override fun variables(): List<String> {
        return tableVariables.workspace(dataSource) {
            select {
                where {
                    "mode" eq true
                }
                rows("name")
            }
        }.map {
            getString("name")
        }
    }

    companion object {

        private val cacheUserId = ConcurrentHashMap<String, Long>()
        private val cacheQuestId = ConcurrentHashMap<String, MutableMap<String, Long>>()

        @SubscribeEvent
        internal fun e(e: PlayerQuitEvent) {
            cacheUserId.remove(e.player.name)
            cacheQuestId.remove(e.player.name)
        }
    }
}