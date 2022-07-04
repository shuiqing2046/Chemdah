package ink.ptms.chemdah.core.quest.objective.other

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Task

/**
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
object IPlayerData : APlayerData() {

    override val name = "player data"

    override fun getValue(profile: PlayerProfile, task: Task, key: String): String {
        return profile.persistentDataContainer[key].toString()
    }
}