package ink.ptms.chemdah.database

import ink.ptms.chemdah.Chemdah
import ink.ptms.chemdah.core.PlayerProfile
import io.izzel.taboolib.module.db.sql.*
import org.bukkit.entity.Player

/**
 * Chemdah
 * ink.ptms.chemdah.database.DatabaseSQL
 *
 * @author sky
 * @since 2021/3/5 3:51 下午
 */
class DatabaseSQL : Database {

    val p = Chemdah.conf.getString("database.source.SQL.table")

    val host = SQLHost(Chemdah.conf.getConfigurationSection("database.source.SQL"), Chemdah.plugin, true)

    // id | user | uuid | time
    val tableUser = SQLTable("${p}_user")
        .column(SQLColumn.PRIMARY_KEY_ID)
        .column(SQLColumnType.VARCHAR.toColumn(32, "user").columnOptions(SQLColumnOption.UNIQUE_KEY))
        .column(SQLColumnType.VARCHAR.toColumn(64, "uuid").columnOptions(SQLColumnOption.UNIQUE_KEY))
        .column(SQLColumnType.DATE.toColumn("time"))!!

    // id | user | quest | data
    val tableQuest = SQLTable("${p}_quest")
        .column(SQLColumn.PRIMARY_KEY_ID)
        .column(SQLColumnType.INT.toColumn(16, "user").columnOptions(SQLColumnOption.KEY))
        .column(SQLColumnType.VARCHAR.toColumn(32, "quest").columnOptions(SQLColumnOption.KEY))
        .column(SQLColumnType.BLOB.toColumn("data"))

    override fun select(player: Player): PlayerProfile {
        TODO("Not yet implemented")
    }

    override fun update(player: Player, playerProfileProfile: PlayerProfile) {
        TODO("Not yet implemented")
    }
}