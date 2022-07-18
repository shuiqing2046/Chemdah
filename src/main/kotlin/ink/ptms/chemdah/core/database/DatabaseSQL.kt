package ink.ptms.chemdah.core.database

import ink.ptms.chemdah.Chemdah
import ink.ptms.chemdah.api.event.collect.PlayerEvents
import ink.ptms.chemdah.core.DataContainer
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Quest
import org.bukkit.entity.Player
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
@Suppress("DuplicatedCode")
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
        tableUser.update(dataSource) {
            where("id" eq userId)
            set("time", Date())
        }
    }

    fun getUserId(player: Player): Long {
        if (cacheUserId.containsKey(player.name)) {
            return cacheUserId[player.name]!!
        }
        val userId = tableUser.select(dataSource) {
            rows("id")
            when (UserIndex.INSTANCE) {
                UserIndex.NAME -> where("uuid" eq player.name)
                UserIndex.UUID -> where("uuid" eq player.uniqueId.toString())
            }
            limit(1)
        }.firstOrNull { getLong("id") } ?: -1L
        cacheUserId[player.name] = userId
        return userId
    }

    fun getQuestId(player: Player, quest: Quest): Long {
        val map = cacheQuestId.computeIfAbsent(player.name) { HashMap() }
        if (map.containsKey(quest.id)) {
            return map[quest.id]!!
        }
        val questId = tableQuest.select(dataSource) {
            rows("id")
            where("user" eq getUserId(player) and ("quest" eq quest.id))
            limit(1)
        }.firstOrNull { getLong("id") } ?: -1L
        map[quest.id] = questId
        return questId
    }

    fun PlayerProfile.init(): PlayerProfile {
        tableUserData.select(dataSource) {
            rows("key", "value")
            where("user" eq getUserId(player) and ("mode" eq 1))
        }.map {
            getString("key") to getString("value")
        }.forEach {
            persistentDataContainer.unchanged { this[it.first] = it.second }
        }
        val quests = HashMap<String, DataContainer>()
        tableQuest.select(dataSource) {
            rows("${tableQuest.name}.quest", "${tableQuestData.name}.key", "${tableQuestData.name}.value")
            where("user" eq getUserId(player) and ("${tableQuest.name}.mode" eq 1) and ("${tableQuestData.name}.mode" eq 1))
            innerJoin(tableQuestData.name) {
                where { "${tableQuest.name}.id" eq pre("${tableQuestData.name}.quest") }
            }
        }.map {
            getString("${tableQuest.name}.quest") to (getString("${tableQuestData.name}.key") to getString("${tableQuestData.name}.value"))
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
            // 非临时变量
            if (data.changed && !key.startsWith("__")) {
                if (tableUserData.find(dataSource) { where("user" eq id and ("key" eq key)) }) {
                    tableUserData.update(dataSource) {
                        where("user" eq id and ("key" eq key))
                        set("value", data.data)
                        set("mode", 1)
                    }
                } else {
                    tableUserData.insert(dataSource, "user", "key", "value", "mode") { value(id, key, data.data, 1) }
                }
            }
        }
        if (persistentDataContainer.drops.isNotEmpty()) {
            tableUserData.update(dataSource) {
                where { "user" eq id and ("key" inside persistentDataContainer.drops.toTypedArray()) }
                set("value", null)
                set("mode", 0)
            }
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
                // 在2021年9月7日的升级测试中发现，一旦任务完成后再次接受将不会被同步数据，这可能是由上个版本的 SQL 写法尚未进行详细测试导致
                // 添加下面的代码使任务恢复到接受状态
                tableQuest.update(dataSource) {
                    where("id" eq questId and ("user" eq id))
                    set("mode", 1)
                }
                // 对任务数据进行更新
                quest.persistentDataContainer.forEach { (key, data) ->
                    // 非临时变量
                    if (data.changed && !key.startsWith("__")) {
                        if (tableQuestData.find(dataSource) { where("quest" eq questId and ("key" eq key)) }) {
                            tableQuestData.update(dataSource) {
                                where("quest" eq questId and ("key" eq key))
                                set("value", data.data)
                                set("mode", 1)
                            }
                        } else {
                            tableQuestData.insert(dataSource, "quest", "key", "value", "mode") { value(questId, key, data.data, 1) }
                        }
                    }
                }
                // 对丢弃对数据进行删除
                if (quest.persistentDataContainer.drops.isNotEmpty()) {
                    tableQuestData.update(dataSource) {
                        where { "quest" eq questId and ("key" inside quest.persistentDataContainer.drops.toTypedArray()) }
                        set("value", null)
                        set("mode", 0)
                    }
                }
                quest.persistentDataContainer.flush()
            }
        }
    }

    fun PlayerProfile.createUser(player: Player): CompletableFuture<Long> {
        val future = CompletableFuture<Long>()
        tableUser.insert(dataSource, "name", "uuid", "time") {
            value(player.name, player.uniqueId.toString(), Date())
            onFinally {
                val userId = generatedKeys.run {
                    next()
                    Coerce.toLong(getObject(1))
                }
                cacheUserId[player.name] = userId
                // 存在数据
                if (persistentDataContainer.isNotEmpty()) {
                    tableUserData.insert(dataSource, "user", "key", "value", "mode") {
                        persistentDataContainer.forEach { (k, v) ->
                            // 非临时变量
                            if (!k.startsWith("__")) {
                                value(userId, k, v.data, 1)
                            }
                        }
                    }
                }
                persistentDataContainer.flush()
                getQuests().forEach { player.createQuest(userId, it) }
                future.complete(userId)
            }
        }
        return future
    }

    fun Player.createQuest(userId: Long, quest: Quest) {
        tableQuest.insert(dataSource, "user", "quest", "mode") {
            value(userId, quest.id, 1)
            onFinally {
                val questId = generatedKeys.run {
                    next()
                    Coerce.toLong(getObject(1))
                }
                cacheQuestId.computeIfAbsent(name) { HashMap() }[quest.id] = questId
                // 存在数据
                if (quest.persistentDataContainer.isNotEmpty()) {
                    tableQuestData.insert(dataSource, "quest", "key", "value", "mode") {
                        quest.persistentDataContainer.forEach { (k, v) ->
                            // 非临时变量
                            if (!k.startsWith("__")) {
                                value(questId, k, v.data, 1)
                            }
                        }
                    }
                }
                quest.persistentDataContainer.flush()
            }
        }
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
        tableQuest.update(dataSource) {
            where { "user" eq getUserId(player) and ("quest" eq quest.id) }
            set("mode", 0)
        }
        tableQuestData.update(dataSource) {
            where { "quest" eq questId }
            set("mode", 0)
        }
    }

    override fun selectVariable0(key: String): String? {
        return tableVariables.select(dataSource) {
            rows("data")
            where { "name" eq key and ("mode" eq true) }
            limit(1)
        }.firstOrNull {
            getString("data")
        }
    }

    override fun updateVariable0(key: String, value: String) {
        if (tableVariables.find(dataSource) { where("name" eq key) }) {
            tableVariables.update(dataSource) {
                where { "name" eq key }
                set("data", value)
                set("mode", true)
            }
        } else {
            tableVariables.insert(dataSource, "name", "data", "mode") { value(key, value, true) }
        }
    }

    override fun releaseVariable0(key: String) {
        tableVariables.update(dataSource) {
            where { "name" eq key }
            set("data", null)
            set("mode", false)
        }
    }

    override fun variables(): List<String> {
        return tableVariables.select(dataSource) {
            rows("name")
            where { "mode" eq true }
        }.map {
            getString("name")
        }
    }

    companion object {

        private val cacheUserId = ConcurrentHashMap<String, Long>()
        private val cacheQuestId = ConcurrentHashMap<String, MutableMap<String, Long>>()

        @SubscribeEvent
        internal fun onReleased(e: PlayerEvents.Released) {
            cacheUserId.remove(e.player.name)
            cacheQuestId.remove(e.player.name)
        }
    }
}