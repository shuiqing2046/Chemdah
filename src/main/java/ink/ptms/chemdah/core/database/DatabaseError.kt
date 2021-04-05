package ink.ptms.chemdah.core.database

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Quest
import org.bukkit.entity.Player

/**
 * Chemdah
 * ink.ptms.chemdah.database.DatabaseSQL
 *
 * @author sky
 * @since 2021/3/5 3:51 下午
 */
class DatabaseError(val cause: Throwable) : Database() {

    override fun select(player: Player): PlayerProfile {
        throw IllegalAccessError("Database initialization failed: ${cause.localizedMessage}")
    }

    override fun update(player: Player, playerProfile: PlayerProfile) {
        throw IllegalAccessError("Database initialization failed: ${cause.localizedMessage}")
    }

    override fun releaseQuest(player: Player, playerProfile: PlayerProfile, quest: Quest) {
        throw IllegalAccessError("Database initialization failed: ${cause.localizedMessage}")
    }

    override fun selectVariable0(key: String): String? {
        throw IllegalAccessError("Database initialization failed: ${cause.localizedMessage}")
    }

    override fun updateVariable0(key: String, value: String) {
        throw IllegalAccessError("Database initialization failed: ${cause.localizedMessage}")
    }

    override fun releaseVariable0(key: String) {
        throw IllegalAccessError("Database initialization failed: ${cause.localizedMessage}")
    }

    override fun variables(): List<String> {
        throw IllegalAccessError("Database initialization failed: ${cause.localizedMessage}")
    }
}