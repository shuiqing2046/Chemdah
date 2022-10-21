package ink.ptms.chemdah.core.database

import ink.ptms.chemdah.Chemdah
import ink.ptms.chemdah.api.event.collect.PlayerEvents
import ink.ptms.chemdah.core.DataContainer
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Quest
import org.bukkit.entity.Player
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.getDataFolder
import taboolib.module.database.ColumnTypeSQLite
import taboolib.module.database.Table
import taboolib.module.database.getHost
import java.io.File
import java.util.*
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
class DatabaseSQLite : Database() {

    val host = File(getDataFolder(), "data.db").getHost()

    val name: String
        get() = Chemdah.conf.getString("database.source.SQL.table", "chemdah")!!

    val tableUserData = Table("${name}_user_data", host) {
        // 对应玩家
        add("user") {
            type(ColumnTypeSQLite.TEXT, 36)
        }
        add("key") {
            type(ColumnTypeSQLite.TEXT, 64)
        }
        add("value") {
            type(ColumnTypeSQLite.TEXT, 64)
        }
        add("mode") {
            type(ColumnTypeSQLite.INTEGER)
        }
        primaryKeyForLegacy += arrayOf("user", "key")
    }

    val tableQuest = Table("${name}_quest", host) {
        // 任务 UUID
        add("id") {
            type(ColumnTypeSQLite.TEXT, 36)
        }
        // 对应玩家
        add("user") {
            type(ColumnTypeSQLite.TEXT, 36)
        }
        // 任务名称
        add("quest") {
            type(ColumnTypeSQLite.TEXT, 36)
        }
        add("mode") {
            type(ColumnTypeSQLite.INTEGER)
        }
        primaryKeyForLegacy += arrayOf("id", "user", "quest")
    }

    val tableQuestData = Table("${name}_quest_data", host) {
        // 任务 UUID
        add("id") {
            type(ColumnTypeSQLite.TEXT, 36)
        }
        add("key") {
            type(ColumnTypeSQLite.TEXT, 64)
        }
        add("value") {
            type(ColumnTypeSQLite.TEXT, 64)
        }
        add("mode") {
            type(ColumnTypeSQLite.INTEGER)
        }
        primaryKeyForLegacy += arrayOf("id", "key")
    }

    val tableVariables = Table("${name}_variables", host) {
        add("name") {
            type(ColumnTypeSQLite.TEXT, 64)
        }
        add("data") {
            type(ColumnTypeSQLite.TEXT, 64)
        }
        add("mode") {
            type(ColumnTypeSQLite.INTEGER)
        }
        primaryKeyForLegacy += "name"
    }

    val dataSource: DataSource by lazy {
        host.createDataSource()
    }

    init {
        tableQuest.workspace(dataSource) { createTable() }.run()
        tableUserData.workspace(dataSource) { createTable() }.run()
        tableQuestData.workspace(dataSource) { createTable() }.run()
        tableVariables.workspace(dataSource) { createTable() }.run()
    }

    /**
     * 获取玩家标识
     */
    fun getUserId(player: Player): String {
        return when (UserIndex.INSTANCE) {
            UserIndex.NAME -> player.name
            UserIndex.UUID -> player.uniqueId.toString()
        }
    }

    /**
     * 获取任务对应 UUID
     */
    fun getQuestId(player: Player, quest: Quest): String? {
        val map = cacheQuestId.computeIfAbsent(player.name) { HashMap() }
        if (map.containsKey(quest.id)) {
            return map[quest.id]!!
        }
        val questId = tableQuest.select(dataSource) {
            rows("id")
            where("user" eq getUserId(player) and ("quest" eq quest.id))
            limit(1)
        }.firstOrNull { getString("id") } ?: return null
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
            rows("id", "quest")
            where("user" eq getUserId(player) and ("mode" eq 1))
        }.forEach {
            val id = getString("id")
            val quest = getString("quest")
            tableQuestData.select(dataSource) {
                rows("key", "value")
                where("id" eq id and ("mode" eq 1))
            }.forEach {
                quests.computeIfAbsent(quest) { DataContainer() }.unchanged { set(getString("key"), getString("value")) }
            }
        }
        quests.forEach { registerQuest(Quest(it.key, this, it.value), newQuest = false) }
        return this
    }

    fun PlayerProfile.update(player: Player) {
        val id = getUserId(player)
        persistentDataContainer.forEach { (key, data) ->
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
                if (questId == null) {
                    player.createQuest(quest)
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
                    if (data.changed && !key.startsWith("__")) {
                        if (tableQuestData.find(dataSource) { where("id" eq questId and ("key" eq key)) }) {
                            tableQuestData.update(dataSource) {
                                where("id" eq questId and ("key" eq key))
                                set("value", data.data)
                                set("mode", 1)
                            }
                        } else {
                            tableQuestData.insert(dataSource, "id", "key", "value", "mode") { value(questId, key, data.data, 1) }
                        }
                    }
                }
                // 对丢弃对数据进行删除
                if (quest.persistentDataContainer.drops.isNotEmpty()) {
                    tableQuestData.update(dataSource) {
                        where { "id" eq questId and ("key" inside quest.persistentDataContainer.drops.toTypedArray()) }
                        set("value", null)
                        set("mode", 0)
                    }
                }
                quest.persistentDataContainer.flush()
            }
        }
    }

    fun Player.createQuest(quest: Quest) {
        val uuid = UUID.randomUUID().toString()
        tableQuest.insert(dataSource, "id", "user", "quest", "mode") {
            value(uuid, getUserId(this@createQuest), quest.id, 1)
            onFinally {
                cacheQuestId.computeIfAbsent(name) { HashMap() }[quest.id] = uuid
                if (quest.persistentDataContainer.isNotEmpty()) {
                    tableQuestData.insert(dataSource, "id", "key", "value", "mode") {
                        quest.persistentDataContainer.forEach { (k, v) ->
                            if (!k.startsWith("__")) {
                                value(uuid, k, v.data, 1)
                            }
                        }
                    }
                }
                quest.persistentDataContainer.flush()
            }
        }
    }

    override fun select(player: Player): PlayerProfile {
        return PlayerProfile(player.uniqueId).init()
    }

    override fun update(player: Player, playerProfile: PlayerProfile) {
        playerProfile.update(player)
        playerProfile.updateQuest(player)
    }

    override fun releaseQuest(player: Player, playerProfile: PlayerProfile, quest: Quest) {
        val questId = getQuestId(player, quest) ?: return
        tableQuest.update(dataSource) {
            where { "user" eq getUserId(player) and ("quest" eq quest.id) }
            set("mode", 0)
        }
        tableQuestData.update(dataSource) {
            where { "id" eq questId }
            set("mode", 0)
        }
    }

    override fun selectVariable0(key: String): String? {
        return tableVariables.select(dataSource) {
            rows("data")
            where { "name" eq key and ("mode" eq 1) }
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
                set("mode", 1)
            }
        } else {
            tableVariables.insert(dataSource, "name", "data", "mode") { value(key, value, 1) }
        }
    }

    override fun releaseVariable0(key: String) {
        tableVariables.update(dataSource) {
            where { "name" eq key }
            set("data", null)
            set("mode", 0)
        }
    }

    override fun variables(): List<String> {
        return tableVariables.select(dataSource) {
            rows("name")
            where { "mode" eq 1 }
        }.map {
            getString("name")
        }
    }

    companion object {

        // <玩家, <任务名称, 任务UUID>>
        private val cacheQuestId = ConcurrentHashMap<String, MutableMap<String, String>>()

        @SubscribeEvent
        private fun onReleased(e: PlayerEvents.Released) {
            cacheQuestId.remove(e.player.name)
        }
    }
}