package ink.ptms.chemdah.core.quest.objective.placeholderapi

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Task
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import ink.ptms.chemdah.core.quest.objective.Progress
import ink.ptms.chemdah.core.quest.objective.Progress.Companion.toProgress
import ink.ptms.chemdah.core.quest.objective.other.APlayerData
import org.bukkit.event.Event
import taboolib.common5.Coerce
import taboolib.platform.compat.replacePlaceholder

/**
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
object PPlaceholderAPI : APlayerData() {

    override val name = "placeholder api"

    override fun getValue(profile: PlayerProfile, task: Task, key: String): String {
        return key.replacePlaceholder(profile.player)
    }
}