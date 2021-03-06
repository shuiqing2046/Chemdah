package ink.ptms.chemdah.database

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
class DatabaseLocal : Database {

    override fun select(player: Player): PlayerProfile {
        TODO("Not yet implemented")
    }

    override fun update(player: Player, playerProfile: PlayerProfile) {
        TODO("Not yet implemented")
    }

    override fun releaseQuest(player: Player, playerProfile: PlayerProfile, quest: Quest) {
        TODO("Not yet implemented")
    }
}