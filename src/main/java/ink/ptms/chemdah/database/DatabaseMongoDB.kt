package ink.ptms.chemdah.database

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Id
import org.bukkit.entity.Player

/**
 * Chemdah
 * ink.ptms.chemdah.database.DatabaseSQL
 *
 * @author sky
 * @since 2021/3/5 3:51 下午
 */
class DatabaseMongoDB : Database {

    override fun select(player: Player): PlayerProfile {
        TODO("Not yet implemented")
    }

    override fun update(player: Player, playerProfileProfile: PlayerProfile) {
        TODO("Not yet implemented")
    }
}